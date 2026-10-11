-- Add Explore metadata to the existing catalog; run once after schema inspection.
-- MySQL 8.0 / InnoDB; preserve all old style IDs and preferences.
USE fashion_ai;
CREATE TABLE style_groups (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
 code VARCHAR(50) NOT NULL,
 name_en VARCHAR(100) NOT NULL,
 name_vi VARCHAR(100) NOT NULL,
 sort_order INT UNSIGNED NOT NULL DEFAULT 0,
 PRIMARY KEY (id), UNIQUE KEY uq_style_groups_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
ALTER TABLE styles
 ADD COLUMN slug VARCHAR(100) NULL,
 ADD COLUMN group_id BIGINT UNSIGNED NULL,
 ADD COLUMN image_url VARCHAR(1000) NULL,
 ADD COLUMN is_explore_enabled BOOLEAN NOT NULL DEFAULT FALSE,
 ADD UNIQUE KEY uq_styles_slug (slug),
 ADD INDEX idx_styles_explore_group (is_explore_enabled, group_id),
 ADD CONSTRAINT fk_styles_group FOREIGN KEY (group_id) REFERENCES style_groups(id) ON DELETE RESTRICT,
 ADD CONSTRAINT ck_styles_cloudinary_image CHECK (image_url IS NULL OR image_url LIKE 'https://res.cloudinary.com/%');
