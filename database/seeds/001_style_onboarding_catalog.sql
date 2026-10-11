-- PROPOSED ONLY: do not execute until live schema constraints are verified.
-- Requires UNIQUE(code) on languages/styles/colors/occasions and translation
-- PRIMARY KEY (catalog_id, language_id), with valid catalog/language foreign keys.
-- Requires InnoDB and UTF-8-capable name/native_name translation columns.
-- 9 styles, 8 colors, 6 occasions, 46 vi/en translation rows on empty catalogs.
-- Existing rows, active/default flags and translations are preserved on reruns.
-- No fixed IDs; existing catalog/language IDs are resolved by code.
-- languages vi/en are inserted only when absent; no existing default is changed.
-- Execute serially on one connection. If any statement fails, STOP and ROLLBACK.
-- Workbench must not continue to COMMIT after an error.
-- No writes to users/user_profiles or any user preference tables.

USE fashion_ai;
SET NAMES utf8mb4;
START TRANSACTION;

INSERT INTO languages (code, name, native_name, is_default, is_active)
VALUES ('vi', 'Vietnamese', 'Tiếng Việt', FALSE, TRUE),
       ('en', 'English', 'English', FALSE, TRUE)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO styles (code, is_active, sort_order)
VALUES
    ('MINIMALIST', TRUE, 1),
    ('ELEGANT', TRUE, 2),
    ('CASUAL', TRUE, 3),
    ('STREETWEAR', TRUE, 4),
    ('ROMANTIC', TRUE, 5),
    ('SPORTY', TRUE, 6),
    ('OLD_MONEY', TRUE, 7),
    ('VINTAGE', TRUE, 8),
    ('FEMININE', TRUE, 9)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO style_translations (style_id, language_id, name)
SELECT catalog.id, lang.id,
       CASE lang.code WHEN 'vi' THEN seed.name_vi WHEN 'en' THEN seed.name_en END
FROM (
    SELECT 'MINIMALIST' AS code, 'Tối giản' AS name_vi, 'Minimalist' AS name_en
    UNION ALL SELECT 'ELEGANT', 'Thanh lịch', 'Elegant'
    UNION ALL SELECT 'CASUAL', 'Thường ngày', 'Casual'
    UNION ALL SELECT 'STREETWEAR', 'Đường phố', 'Streetwear'
    UNION ALL SELECT 'ROMANTIC', 'Lãng mạn', 'Romantic'
    UNION ALL SELECT 'SPORTY', 'Thể thao', 'Sporty'
    UNION ALL SELECT 'OLD_MONEY', 'Cổ điển sang trọng', 'Old money'
    UNION ALL SELECT 'VINTAGE', 'Hoài cổ', 'Vintage'
    UNION ALL SELECT 'FEMININE', 'Nữ tính', 'Feminine'
) AS seed
JOIN styles catalog ON catalog.code = seed.code
CROSS JOIN languages lang
WHERE lang.code IN ('vi', 'en')
ON DUPLICATE KEY UPDATE name = style_translations.name;

INSERT INTO colors (code, hex_code, is_active, sort_order)
VALUES
    ('BLACK', '#191816', TRUE, 1),
    ('WHITE', '#FFFFFF', TRUE, 2),
    ('IVORY', '#FAF8F4', TRUE, 3),
    ('BEIGE', '#D8C3A5', TRUE, 4),
    ('BROWN', '#8B684F', TRUE, 5),
    ('BLUE', '#547AA5', TRUE, 6),
    ('PINK', '#DCA6B2', TRUE, 7),
    ('GREEN', '#728C69', TRUE, 8)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO color_translations (color_id, language_id, name)
SELECT catalog.id, lang.id,
       CASE lang.code WHEN 'vi' THEN seed.name_vi WHEN 'en' THEN seed.name_en END
FROM (
    SELECT 'BLACK' AS code, 'Đen' AS name_vi, 'Black' AS name_en
    UNION ALL SELECT 'WHITE', 'Trắng', 'White'
    UNION ALL SELECT 'IVORY', 'Trắng ngà', 'Ivory'
    UNION ALL SELECT 'BEIGE', 'Be', 'Beige'
    UNION ALL SELECT 'BROWN', 'Nâu', 'Brown'
    UNION ALL SELECT 'BLUE', 'Xanh dương', 'Blue'
    UNION ALL SELECT 'PINK', 'Hồng', 'Pink'
    UNION ALL SELECT 'GREEN', 'Xanh lá', 'Green'
) AS seed
JOIN colors catalog ON catalog.code = seed.code
CROSS JOIN languages lang
WHERE lang.code IN ('vi', 'en')
ON DUPLICATE KEY UPDATE name = color_translations.name;

INSERT INTO occasions (code, is_active, sort_order)
VALUES
    ('SCHOOL', TRUE, 1),
    ('WORK', TRUE, 2),
    ('DAILY', TRUE, 3),
    ('OUTING', TRUE, 4),
    ('PARTY', TRUE, 5),
    ('FORMAL_EVENT', TRUE, 6)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO occasion_translations (occasion_id, language_id, name)
SELECT catalog.id, lang.id,
       CASE lang.code WHEN 'vi' THEN seed.name_vi WHEN 'en' THEN seed.name_en END
FROM (
    SELECT 'SCHOOL' AS code, 'Đi học' AS name_vi, 'School' AS name_en
    UNION ALL SELECT 'WORK', 'Đi làm', 'Work'
    UNION ALL SELECT 'DAILY', 'Sinh hoạt thường ngày', 'Everyday'
    UNION ALL SELECT 'OUTING', 'Đi chơi', 'Going out'
    UNION ALL SELECT 'PARTY', 'Dự tiệc', 'Party'
    UNION ALL SELECT 'FORMAL_EVENT', 'Sự kiện trang trọng', 'Formal event'
) AS seed
JOIN occasions catalog ON catalog.code = seed.code
CROSS JOIN languages lang
WHERE lang.code IN ('vi', 'en')
ON DUPLICATE KEY UPDATE name = occasion_translations.name;

COMMIT;

-- Expected totals on the currently empty catalogs: 9, 8, 6, 18, 16, 12.
