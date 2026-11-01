-- Additive reviewed visual content; original style_guides and cover assets are preserved.
CREATE TABLE IF NOT EXISTS style_visual_profiles (
 style_id BIGINT UNSIGNED NOT NULL PRIMARY KEY,
 content JSON NOT NULL,
 provenance JSON NOT NULL,
 content_version INT NOT NULL DEFAULT 1,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 CONSTRAINT fk_visual_profile_style FOREIGN KEY(style_id) REFERENCES styles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
