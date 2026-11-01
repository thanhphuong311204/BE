USE fashion_ai;
-- Additive migration, following migrations 001-007. users.id is BIGINT UNSIGNED.
CREATE TABLE chat_conversations (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT UNSIGNED NOT NULL,
 title VARCHAR(120) NOT NULL,
 created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 CONSTRAINT fk_chat_conversation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
 INDEX idx_chat_conversations_owner_updated (user_id, updated_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE chat_messages (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 conversation_id BIGINT UNSIGNED NOT NULL,
 role VARCHAR(9) NOT NULL,
 content TEXT NOT NULL,
 created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 CONSTRAINT fk_chat_message_conversation FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
 CONSTRAINT chk_chat_message_role CHECK (role IN ('USER','ASSISTANT')),
 INDEX idx_chat_messages_conversation_id (conversation_id, id),
 INDEX idx_chat_messages_conversation_created (conversation_id, created_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
