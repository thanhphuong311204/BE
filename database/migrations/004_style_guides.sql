-- Minimal structured editorial content; no changes to users/Profile/Onboarding.
USE fashion_ai;
CREATE TABLE style_guides (
 style_id BIGINT UNSIGNED NOT NULL,
 language_id BIGINT UNSIGNED NOT NULL,
 content JSON NOT NULL,
 content_version INT UNSIGNED NOT NULL DEFAULT 1,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY (style_id,language_id),
 CONSTRAINT fk_style_guides_style FOREIGN KEY (style_id) REFERENCES styles(id) ON DELETE CASCADE,
 CONSTRAINT fk_style_guides_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
