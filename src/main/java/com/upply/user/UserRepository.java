package com.upply.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("select u from User u where u.id = ?#{principal.getId()}")
    Optional<User> getCurrentUser();

    @Query("""
        SELECT us.name
        FROM User u
        JOIN u.userSkills us
        WHERE u.id = :userId
    """)
    List<String> findUserSkillNames(@Param("userId") Long userId);
}
