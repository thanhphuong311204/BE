-- Inspect existing schema first; additive gallery independent of edited guide JSON.
USE fashion_ai;
CREATE TABLE IF NOT EXISTS style_outfit_images (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
 style_id BIGINT UNSIGNED NOT NULL,
 outfit_key VARCHAR(100) NOT NULL,
 image_url VARCHAR(1000) NOT NULL,
 public_id VARCHAR(255) NOT NULL,
 caption VARCHAR(500) NOT NULL DEFAULT '',
 sha256 CHAR(64) NOT NULL,
 provenance VARCHAR(1000) NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uq_style_outfit_image_hash (style_id,sha256),
 INDEX idx_style_outfit_key (style_id,outfit_key),
 CONSTRAINT fk_style_outfit_image_style FOREIGN KEY (style_id) REFERENCES styles(id) ON DELETE CASCADE,
 CONSTRAINT ck_style_outfit_cloudinary CHECK (image_url LIKE 'https://res.cloudinary.com/%')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
