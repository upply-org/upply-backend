package com.upply.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Integer> {

    Optional<ActivationToken> findByToken(String token);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE ActivationToken T
        SET T.used = true 
        WHERE T.user.id = :userId AND T.used = false 
    """)
    void markAllTokensAsUsedForUser(Long userId);
}