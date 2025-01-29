package com.basic.oauth.repository;

import com.basic.oauth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndStatus(String username, String status);
    boolean existsByUsername(String username);
}
