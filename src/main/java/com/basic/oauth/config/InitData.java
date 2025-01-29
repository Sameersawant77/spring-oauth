package com.basic.oauth.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.provisioning.UserDetailsManager;

import com.basic.oauth.model.RSAKeyPair;
import com.basic.oauth.repository.RSAKeyRepository;

@Configuration
public class InitData {

    @Bean
    public ApplicationRunner applicationRunner(RegisteredClientRepository registeredClientRepository, UserDetailsManager userDetailsManager, RSAKeyRepository rsaKeyRepository) {
        return args -> {
            if (Objects.isNull(registeredClientRepository.findByClientId("netc-portal"))) {
                RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("netc-portal")
                        .clientSecret("secret")
                        .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUri("http://127.0.0.1:8081/login/aouth2/code/netc-portal")
                        .postLogoutRedirectUri("http:127.0.0.1:8080")
                        .scope(OidcScopes.OPENID)
                        .scope(OidcScopes.PROFILE)
                        .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                        .build();

                registeredClientRepository.save(registeredClient);
            }

            if (rsaKeyRepository.findAllKeys().isEmpty()) {
                var key = generateRSAKeys();
                rsaKeyRepository.save(
                        new RSAKeyPair(
                                UUID.randomUUID().toString(),
                                (RSAPrivateKey) key.getPrivate(),
                                (RSAPublicKey) key.getPublic(),
                                OffsetDateTime.now()
                        )
                );
            }

        };
    }

    private static KeyPair generateRSAKeys() {
        KeyPair keyPair;

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception exception) {
            throw new RuntimeException("failed to create keypair!");
        }

        return keyPair;
    }

}
