CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    id BIGINT NOT NULL AUTO_INCREMENT,
    conversation_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_conversation_timestamp (conversation_id, `timestamp`)
);
