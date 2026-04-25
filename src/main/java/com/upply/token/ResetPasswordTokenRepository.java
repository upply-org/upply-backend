package com.upply.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, Integer> {

    Optional<ResetPasswordToken> findByToken(String token);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE ResetPasswordToken T
        SET T.used = true 
        WHERE T.user.id = :userId AND T.used = false 
    """)
    void markAllTokensAsUsedForUser(Long userId);
}