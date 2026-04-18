package com.upply.user;

import com.upply.config.KafkaConfig;
import com.upply.profile.skill.SkillRepository;
import com.upply.user.dto.SkillEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserSkillEventConsumer {
    private final UserService userService;
    private UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final VectorStore skillVectorStore;

    public UserSkillEventConsumer(UserService userService, UserRepository userRepository, SkillRepository skillRepository,@Qualifier("userSkillsVectorStore") VectorStore skillVectorStore) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.skillVectorStore = skillVectorStore;
    }

    @KafkaListener(topics = KafkaConfig.UserSkillsEmbeddingTopic,
    groupId = "user-skill-event-consumer")
    public void consumeSkillEvent(SkillEvent event) {
        log.info("Received Skill Event: {}", event);
        userRepository.findById(event.getUserId())
                .ifPresentOrElse(
                        user -> storeUserSkillsEmbedding(user.getId(), buildUserContext(user)),
                        ()-> log.warn(("Skipping skill embedding event for missing user: {}\", event.getUserId()"))
                );
    }

    private String buildUserContext(User user) {
        StringBuilder ctx = new StringBuilder();
        List<String> skills = userRepository.findUserSkillNames(user.getId());
        String skillNames = String.join(", ", skills);
        ctx.append("User Skills: ").append(skillNames).append(".");
        return ctx.toString();
    }

    private void storeUserSkillsEmbedding(Long userId,String ctx){
        Document document = new Document(
                String.valueOf(userId),
                ctx,
                Map.of(
                        "userId", userId.toString()
                )
        );
        skillVectorStore.add(List.of(document));
    }
}
