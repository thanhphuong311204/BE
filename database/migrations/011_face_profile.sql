-- TASK14 additive migration; no user records/images are removed or backfilled.

SET @face_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='user_profiles' AND column_name='confirmed_face_shape')=0, 'ALTER TABLE user_profiles ADD COLUMN confirmed_face_shape VARCHAR(16) NULL', 'SELECT 1');
PREPARE face_stmt FROM @face_ddl; EXECUTE face_stmt; DEALLOCATE PREPARE face_stmt;

SET @face_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='user_profiles' AND column_name='face_confirmation_source')=0, 'ALTER TABLE user_profiles ADD COLUMN face_confirmation_source VARCHAR(16) NULL', 'SELECT 1');
PREPARE face_stmt FROM @face_ddl; EXECUTE face_stmt; DEALLOCATE PREPARE face_stmt;

SET @face_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='user_profiles' AND column_name='face_confirmed_at')=0, 'ALTER TABLE user_profiles ADD COLUMN face_confirmed_at TIMESTAMP NULL', 'SELECT 1');
PREPARE face_stmt FROM @face_ddl; EXECUTE face_stmt; DEALLOCATE PREPARE face_stmt;

SET @face_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='user_profiles' AND column_name='face_shape_updated_at')=0, 'ALTER TABLE user_profiles ADD COLUMN face_shape_updated_at TIMESTAMP NULL', 'SELECT 1');
PREPARE face_stmt FROM @face_ddl; EXECUTE face_stmt; DEALLOCATE PREPARE face_stmt;

CREATE TABLE IF NOT EXISTS face_shape_history (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT UNSIGNED NOT NULL,
 previous_shape VARCHAR(16) NULL,
 confirmed_shape VARCHAR(16) NOT NULL,
 confirmation_source VARCHAR(16) NOT NULL,
 changed_at TIMESTAMP NOT NULL,
 INDEX ix_face_history_owner (user_id,id),
 CONSTRAINT fk_face_history_owner FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
