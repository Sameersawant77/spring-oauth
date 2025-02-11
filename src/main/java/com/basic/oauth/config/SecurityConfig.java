package com.basic.oauth.config;

import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.util.StringUtils;

import com.basic.oauth.service.CustomUserDetailsService;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.servlet.http.HttpServletRequest;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
	
	private final UserDetailsService userDetailsService;
	
	 public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.userDetailsService = customUserDetailsService;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityChain(HttpSecurity httpSecurity, RegisteredClientRepository registeredClientRepository) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(httpSecurity);

        httpSecurity.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .clientAuthentication(authentication -> {
                    authentication.authenticationConverter(new PublicClientRefreshTokenAuthenticationConverter());
                    authentication.authenticationProvider(new PublicClientRefreshProvider(registeredClientRepository));
                })
                .oidc(Customizer.withDefaults()); // enable open id connect 1.0

        httpSecurity.exceptionHandling(exception -> {
            exception.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher((MediaType.TEXT_HTML))
            );
        });

        httpSecurity.oauth2ResourceServer(server -> {
            server.jwt(Customizer.withDefaults());
        });

        return httpSecurity.build();
    }


    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(
                authorize -> authorize.anyRequest().authenticated()
        ).formLogin(Customizer.withDefaults()).authenticationProvider(authenticationProvider());

        return httpSecurity.build();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        //provider.setPasswordEncoder(passwordEncoder());
        provider.setPasswordEncoder(new PlainTextPasswordEncoder());
        return provider;
    }

    @Bean
    public OAuth2AuthorizationService auth2AuthorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    OAuth2TokenCustomizer<JwtEncodingContext> customizer() {
        return context -> {
            if(context.getTokenType().getValue().equals(OidcParameterNames.ID_TOKEN)) {
                Authentication principle = context.getPrincipal();
                Set<String> authorities = new HashSet<>();
                for (GrantedAuthority authority: principle.getAuthorities()) {
                    authorities.add(authority.getAuthority());
                }

                context.getClaims().claim("authorities", authorities);
            }
        };
    }

    @Bean
    NimbusJwtEncoder nimbusJwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncode) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncode);
        jwtGenerator.setJwtCustomizer(customizer());
        OAuth2TokenGenerator<OAuth2RefreshToken> refreshTokenOAuth2TokenGenerator = new CustomOAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, refreshTokenOAuth2TokenGenerator);
    }
    
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
    
    public class PlainTextPasswordEncoder implements PasswordEncoder {
        
        @Override
        public String encode(CharSequence rawPassword) {
            return rawPassword.toString();
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return rawPassword.toString().equals(encodedPassword);
        }
    }

    public final class CustomOAuth2RefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2RefreshToken> {
        private final StringKeyGenerator refreshTokenGenerator = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);

        public CustomOAuth2RefreshTokenGenerator() {
        }

        @Nullable
        public OAuth2RefreshToken generate(OAuth2TokenContext context) {
            if (!OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
                return null;
            }  else {
                Instant issuedAt = Instant.now();
                Instant expiresAt = issuedAt.plus(context.getRegisteredClient().getTokenSettings().getRefreshTokenTimeToLive());
                return new OAuth2RefreshToken(this.refreshTokenGenerator.generateKey(), issuedAt, expiresAt);
            }
        }
    }

    private static final class PublicClientRefreshTokenAuthentication extends OAuth2ClientAuthenticationToken {

        private static final long serialVersionUID = 2853033942660266505L;

		public PublicClientRefreshTokenAuthentication(String clientId) {
            super(clientId, ClientAuthenticationMethod.NONE, null, null);
        }

        public PublicClientRefreshTokenAuthentication(RegisteredClient registeredClient) {
            super(registeredClient, ClientAuthenticationMethod.NONE, null);
        }
    }

    private static final class PublicClientRefreshTokenAuthenticationConverter implements AuthenticationConverter {

        @Override
        public Authentication convert(HttpServletRequest request) {
            String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
            if(!grantType.equals(AuthorizationGrantType.REFRESH_TOKEN.getValue())) {
                return null;
            }

            String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
            if(!StringUtils.hasText(clientId)) {
                return null;
            }

            return new PublicClientRefreshTokenAuthentication(clientId);
        }
    }

    private static final class PublicClientRefreshProvider implements AuthenticationProvider {
        private final RegisteredClientRepository registeredClientRepository;

        private PublicClientRefreshProvider(RegisteredClientRepository registeredClientRepository) {
            this.registeredClientRepository = registeredClientRepository;
        }

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            PublicClientRefreshTokenAuthentication publicClientRefreshTokenAuthentication = (PublicClientRefreshTokenAuthentication) authentication;

            if(!ClientAuthenticationMethod.NONE.equals(publicClientRefreshTokenAuthentication.getClientAuthenticationMethod())) {
                return null;
            }

            String clientId = publicClientRefreshTokenAuthentication.getPrincipal().toString();
            RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);

            if(registeredClient == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_CLIENT,
                        "client is not valid",
                        null
                ));
            }

            if(!registeredClient.getClientAuthenticationMethods().contains(
                    publicClientRefreshTokenAuthentication.getClientAuthenticationMethod()
            )) {
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_CLIENT,
                        "authentication_method is not register with client",
                        null
                ));
            }
            return new PublicClientRefreshTokenAuthentication(registeredClient);
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return PublicClientRefreshTokenAuthentication.class.isAssignableFrom(authentication);
        }
    }
}
