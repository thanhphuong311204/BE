USE fashion_ai;
CREATE TABLE wardrobe_items (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT UNSIGNED NOT NULL,
 name VARCHAR(150) NOT NULL,
 main_category_id BIGINT UNSIGNED NOT NULL,
 sub_category_id BIGINT UNSIGNED NULL,
 color_id BIGINT UNSIGNED NOT NULL,
 image_url VARCHAR(1000) NOT NULL,
 image_public_id VARCHAR(255) NOT NULL,
 notes VARCHAR(1000) NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 CONSTRAINT fk_wardrobe_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
 CONSTRAINT fk_wardrobe_main FOREIGN KEY(main_category_id) REFERENCES clothing_categories(id) ON DELETE RESTRICT,
 CONSTRAINT fk_wardrobe_sub FOREIGN KEY(sub_category_id) REFERENCES clothing_categories(id) ON DELETE RESTRICT,
 CONSTRAINT fk_wardrobe_color FOREIGN KEY(color_id) REFERENCES colors(id) ON DELETE RESTRICT,
 INDEX idx_wardrobe_owner_category_color(user_id,main_category_id,color_id),
 CONSTRAINT chk_wardrobe_cloudinary CHECK(image_url LIKE 'https://res.cloudinary.com/%')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE wardrobe_item_styles (
 item_id BIGINT UNSIGNED NOT NULL,
 style_id BIGINT UNSIGNED NOT NULL,
 PRIMARY KEY(item_id,style_id),
 CONSTRAINT fk_wardrobe_style_item FOREIGN KEY(item_id) REFERENCES wardrobe_items(id) ON DELETE CASCADE,
 CONSTRAINT fk_wardrobe_style FOREIGN KEY(style_id) REFERENCES styles(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
