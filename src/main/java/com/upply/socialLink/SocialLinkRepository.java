package com.upply.socialLink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {

    @Query("select sl from SocialLink sl where sl.id =:id and sl.user.id = ?#{principal.getId()}")
    Optional<SocialLink> findSocialLinkById(Long id);

    @Query("select sl from SocialLink sl where sl.user.id = ?#{principal.getId()}")
    List<SocialLink> findUserSocialLinksByUserId();

    @Transactional
    @Modifying
    @Query("delete from SocialLink sl where sl.id = :id and sl.user.id = ?#{principal.getId()}")
    void deleteSocialLinkById(Long id);
}
