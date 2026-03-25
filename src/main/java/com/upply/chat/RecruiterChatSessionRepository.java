package com.upply.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecruiterChatSessionRepository extends JpaRepository<RecruiterChatSession, Long> {
    @Query("select s from RecruiterChatSession s " +
            "where s.sessionId =:sessionId and s.job.postedBy.id = ?#{principal.getId()}")
    Optional<RecruiterChatSession> findBySessionId(String sessionId);


    @Query("select s from RecruiterChatSession s where s.job.postedBy.id = ?#{principal.getId()} order by s.createdAt DESC ")
    List<RecruiterChatSession> findMySessions();


}
