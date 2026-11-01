CREATE TABLE chat_requests (
 user_id BIGINT UNSIGNED NOT NULL,
 request_id VARCHAR(64) NOT NULL,
 fingerprint VARCHAR(64) NOT NULL,
 response TEXT NULL,
 created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 PRIMARY KEY(user_id,request_id),
 FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);
