-- PROPOSED ONLY: do not execute before reviewing live SHOW CREATE TABLE results.
-- Based on the user-supplied schema, not a direct MySQL inspection.
-- Confirm InnoDB, BIGINT UNSIGNED primary IDs on users/styles/colors/occasions,
-- and absence of all four target tables and of the constraint names below.
-- Run once; CREATE TABLE deliberately fails if a target table already exists.
-- DDL is separate from the seed transaction; it is not rolled back as one batch.
-- No ALTER/DROP, account/profile writes, or automatic preference backfill.
-- Max three styles and valid status/timestamp transitions are enforced by
-- the future backend service, not by a cross-row CHECK constraint here.

USE fashion_ai;

CREATE TABLE user_style_preferences (
    user_id BIGINT UNSIGNED NOT NULL,
    status ENUM('NOT_STARTED', 'SKIPPED', 'COMPLETED')
        NOT NULL DEFAULT 'NOT_STARTED',
    completed_at TIMESTAMP NULL DEFAULT NULL,
    skipped_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_usp_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE user_preferred_styles (
    user_id BIGINT UNSIGNED NOT NULL,
    style_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (user_id, style_id),
    INDEX idx_ups_style (style_id),
    CONSTRAINT fk_ups_preferences FOREIGN KEY (user_id)
        REFERENCES user_style_preferences(user_id)
        ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_ups_style FOREIGN KEY (style_id)
        REFERENCES styles(id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE user_preferred_colors (
    user_id BIGINT UNSIGNED NOT NULL,
    color_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (user_id, color_id),
    INDEX idx_upc_color (color_id),
    CONSTRAINT fk_upc_preferences FOREIGN KEY (user_id)
        REFERENCES user_style_preferences(user_id)
        ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_upc_color FOREIGN KEY (color_id)
        REFERENCES colors(id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE user_preferred_occasions (
    user_id BIGINT UNSIGNED NOT NULL,
    occasion_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (user_id, occasion_id),
    INDEX idx_upo_occasion (occasion_id),
    CONSTRAINT fk_upo_preferences FOREIGN KEY (user_id)
        REFERENCES user_style_preferences(user_id)
        ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_upo_occasion FOREIGN KEY (occasion_id)
        REFERENCES occasions(id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;
