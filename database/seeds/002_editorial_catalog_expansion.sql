-- DRAFT FOR REVIEW ONLY. Do not execute without the user's approval.
-- Existing tables only; no schema changes, no preference/account/profile writes.
-- Based on the supplied schema and user's confirmed 9/8/6 catalog counts.
-- Target: 18 styles, 24 colors, 12 occasions; 36/48/24 vi/en translations.
-- Existing code/name/HEX/active flags are preserved. Only missing/blank
-- style/occasion descriptions are filled. Catalog IDs are resolved by code.
-- InnoDB and UNIQUE(code)/translation composite PK/FKs are prerequisites.
-- Run serially on one connection; STOP and ROLLBACK if any statement fails.
-- Do not allow Workbench to continue to COMMIT after an error.

USE fashion_ai;
SET NAMES utf8mb4;
START TRANSACTION;

INSERT INTO languages(code,name,native_name,is_default,is_active)
VALUES ('vi','Vietnamese','Tiếng Việt',FALSE,TRUE),('en','English','English',FALSE,TRUE)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO styles(code,is_active,sort_order)
VALUES
    ('MINIMALIST',TRUE,1),
    ('ELEGANT',TRUE,2),
    ('CASUAL',TRUE,3),
    ('STREETWEAR',TRUE,4),
    ('ROMANTIC',TRUE,5),
    ('SPORTY',TRUE,6),
    ('OLD_MONEY',TRUE,7),
    ('VINTAGE',TRUE,8),
    ('FEMININE',TRUE,9),
    ('BOHEMIAN',TRUE,10),
    ('PREPPY',TRUE,11),
    ('CLASSIC',TRUE,12),
    ('CHIC',TRUE,13),
    ('MONOCHROME',TRUE,14),
    ('SMART_CASUAL',TRUE,15),
    ('EDGY',TRUE,16),
    ('COQUETTE',TRUE,17),
    ('RESORT',TRUE,18)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO style_translations(style_id,language_id,name,description)
SELECT catalog.id,lang.id,
    CASE lang.code WHEN 'vi' THEN seed.name_vi WHEN 'en' THEN seed.name_en END,
    CASE lang.code WHEN 'vi' THEN seed.description_vi WHEN 'en' THEN seed.description_en END
FROM (
    SELECT 'MINIMALIST' AS code,'Tối giản' AS name_vi,'Minimalist' AS name_en,'Đường nét gọn gàng, ít chi tiết và bảng màu trung tính.' AS description_vi,'Clean lines, restrained details and neutral tones.' AS description_en
    UNION ALL SELECT 'ELEGANT','Thanh lịch','Elegant','Phom dáng chỉn chu, chất liệu mềm và chi tiết tinh tế.','Refined silhouettes, soft fabrics and considered details.'
    UNION ALL SELECT 'CASUAL','Thường ngày','Casual','Thoải mái và linh hoạt với những món đồ dễ phối.','Comfortable, versatile everyday essentials.'
    UNION ALL SELECT 'STREETWEAR','Đường phố','Streetwear','Phom rộng, layering và điểm nhấn mang cảm hứng đường phố.','Relaxed silhouettes, layering and street-inspired accents.'
    UNION ALL SELECT 'ROMANTIC','Lãng mạn','Romantic','Vải rủ, họa tiết hoa và gam màu dịu nhẹ.','Flowing fabrics, floral patterns and soft colors.'
    UNION ALL SELECT 'SPORTY','Thể thao','Sporty','Trang phục năng động, gọn nhẹ và dễ di chuyển.','Active, lightweight pieces designed for movement.'
    UNION ALL SELECT 'OLD_MONEY','Cổ điển sang trọng','Old money','Đồ may đo, chất liệu cổ điển và sự sang trọng tiết chế.','Tailored pieces, classic textures and understated polish.'
    UNION ALL SELECT 'VINTAGE','Hoài cổ','Vintage','Phom dáng và họa tiết lấy cảm hứng từ các thập niên trước.','Silhouettes and prints inspired by earlier decades.'
    UNION ALL SELECT 'FEMININE','Nữ tính','Feminine','Đường nét mềm mại với váy, nếp ly và chi tiết thanh nhã.','Soft lines, skirts, pleats and graceful details.'
    UNION ALL SELECT 'BOHEMIAN','Bohemian phóng khoáng','Bohemian','Chất liệu tự nhiên, phom thoải mái và họa tiết phóng khoáng.','Natural textures, relaxed shapes and expressive patterns.'
    UNION ALL SELECT 'PREPPY','Preppy học đường','Preppy','Áo cổ bẻ, blazer và họa tiết kẻ mang cảm hứng học đường.','Collared shirts, blazers and collegiate checks.'
    UNION ALL SELECT 'CLASSIC','Cổ điển','Classic','Những món đồ vượt thời gian với phom dáng cân đối.','Timeless essentials with balanced silhouettes.'
    UNION ALL SELECT 'CHIC','Chic hiện đại','Chic','Phối đồ hiện đại, gọn gàng với một điểm nhấn nổi bật.','Polished modern styling with one distinctive accent.'
    UNION ALL SELECT 'MONOCHROME','Đơn sắc','Monochrome','Kết hợp nhiều sắc độ của cùng một tông màu.','Layered shades from a single color family.'
    UNION ALL SELECT 'SMART_CASUAL','Lịch sự thoải mái','Smart casual','Cân bằng giữa may đo lịch sự và sự thoải mái hằng ngày.','A balance of tailoring and everyday ease.'
    UNION ALL SELECT 'EDGY','Cá tính','Edgy','Da, denim tối màu và những chi tiết tạo cá tính.','Leather, dark denim and expressive accents.'
    UNION ALL SELECT 'COQUETTE','Coquette duyên dáng','Coquette','Gam pastel, nơ nhỏ và chi tiết mềm mại, duyên dáng.','Pastels, small bows and delicate playful details.'
    UNION ALL SELECT 'RESORT','Nghỉ dưỡng','Resort','Linen, phom thoáng và phụ kiện nhẹ cho kỳ nghỉ.','Breezy linen, relaxed shapes and light holiday accessories.'
) AS seed
JOIN styles catalog ON catalog.code=seed.code
CROSS JOIN languages lang
WHERE lang.code IN ('vi','en')
ON DUPLICATE KEY UPDATE description = CASE
    WHEN style_translations.description IS NULL OR TRIM(style_translations.description)=''
    THEN CASE lang.code WHEN 'vi' THEN seed.description_vi WHEN 'en' THEN seed.description_en END
    ELSE style_translations.description END;

INSERT INTO colors(code,hex_code,is_active,sort_order)
VALUES
    ('BLACK','#191816',TRUE,1),
    ('WHITE','#FFFFFF',TRUE,2),
    ('IVORY','#FAF8F4',TRUE,3),
    ('BEIGE','#D8C3A5',TRUE,4),
    ('BROWN','#8B684F',TRUE,5),
    ('BLUE','#547AA5',TRUE,6),
    ('PINK','#DCA6B2',TRUE,7),
    ('GREEN','#728C69',TRUE,8),
    ('NAVY','#000080',TRUE,9),
    ('CHARCOAL','#36454F',TRUE,10),
    ('LIGHT_GRAY','#D3D3D3',TRUE,11),
    ('SILVER','#C0C0C0',TRUE,12),
    ('CREAM','#FFFDD0',TRUE,13),
    ('CAMEL','#C19A6B',TRUE,14),
    ('CHOCOLATE','#7B3F00',TRUE,15),
    ('TAUPE','#8B8589',TRUE,16),
    ('DUSTY_ROSE','#C08081',TRUE,17),
    ('BLUSH','#F4C2C2',TRUE,18),
    ('BURGUNDY','#800020',TRUE,19),
    ('RED','#FF0000',TRUE,20),
    ('ORANGE','#FFA500',TRUE,21),
    ('MUSTARD','#FFDB58',TRUE,22),
    ('LAVENDER','#E6E6FA',TRUE,23),
    ('OLIVE','#808000',TRUE,24)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO color_translations(color_id,language_id,name)
SELECT catalog.id,lang.id,
    CASE lang.code WHEN 'vi' THEN seed.name_vi WHEN 'en' THEN seed.name_en END
FROM (
    SELECT 'BLACK' AS code,'Đen' AS name_vi,'Black' AS name_en
    UNION ALL SELECT 'WHITE','Trắng','White'
    UNION ALL SELECT 'IVORY','Trắng ngà','Ivory'
    UNION ALL SELECT 'BEIGE','Be','Beige'
    UNION ALL SELECT 'BROWN','Nâu','Brown'
    UNION ALL SELECT 'BLUE','Xanh dương','Blue'
    UNION ALL SELECT 'PINK','Hồng','Pink'
    UNION ALL SELECT 'GREEN','Xanh lá','Green'
    UNION ALL SELECT 'NAVY','Xanh navy','Navy'
    UNION ALL SELECT 'CHARCOAL','Xám than','Charcoal'
    UNION ALL SELECT 'LIGHT_GRAY','Xám nhạt','Light gray'
    UNION ALL SELECT 'SILVER','Bạc','Silver'
    UNION ALL SELECT 'CREAM','Kem','Cream'
    UNION ALL SELECT 'CAMEL','Nâu camel','Camel'
    UNION ALL SELECT 'CHOCOLATE','Nâu chocolate','Chocolate'
    UNION ALL SELECT 'TAUPE','Nâu xám','Taupe'
    UNION ALL SELECT 'DUSTY_ROSE','Hồng đất','Dusty rose'
    UNION ALL SELECT 'BLUSH','Hồng phấn','Blush'
    UNION ALL SELECT 'BURGUNDY','Đỏ rượu vang','Burgundy'
    UNION ALL SELECT 'RED','Đỏ','Red'
    UNION ALL SELECT 'ORANGE','Cam','Orange'
    UNION ALL SELECT 'MUSTARD','Vàng mù tạt','Mustard'
    UNION ALL SELECT 'LAVENDER','Tím lavender','Lavender'
    UNION ALL SELECT 'OLIVE','Xanh olive','Olive'
) AS seed
JOIN colors catalog ON catalog.code=seed.code
CROSS JOIN languages lang
WHERE lang.code IN ('vi','en')
ON DUPLICATE KEY UPDATE name = color_translations.name;

INSERT INTO occasions(code,is_active,sort_order)
VALUES
    ('SCHOOL',TRUE,1),
    ('WORK',TRUE,2),
    ('DAILY',TRUE,3),
    ('OUTING',TRUE,4),
    ('PARTY',TRUE,5),
    ('FORMAL_EVENT',TRUE,6),
    ('DATE',TRUE,7),
    ('TRAVEL',TRUE,8),
    ('WEEKEND',TRUE,9),
    ('SPORT',TRUE,10),
    ('OUTDOOR',TRUE,11),
    ('WEDDING',TRUE,12)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO occasion_translations(occasion_id,language_id,name,description)
SELECT catalog.id,lang.id,
    CASE lang.code WHEN 'vi' THEN seed.name_vi WHEN 'en' THEN seed.name_en END,
    CASE lang.code WHEN 'vi' THEN seed.description_vi WHEN 'en' THEN seed.description_en END
FROM (
    SELECT 'SCHOOL' AS code,'Đi học' AS name_vi,'School' AS name_en,'Thoải mái, gọn gàng và phù hợp môi trường học tập.' AS description_vi,'Comfortable, tidy looks for learning environments.' AS description_en
    UNION ALL SELECT 'WORK','Đi làm','Work','Trang phục chỉn chu phù hợp môi trường làm việc.','Polished outfits suited to your workplace.'
    UNION ALL SELECT 'DAILY','Sinh hoạt thường ngày','Everyday','Những món đồ linh hoạt cho hoạt động hằng ngày.','Versatile pieces for daily routines.'
    UNION ALL SELECT 'OUTING','Đi chơi','Going out','Phối đồ thoải mái để gặp bạn bè và dạo phố.','Easy outfits for meeting friends and exploring the city.'
    UNION ALL SELECT 'PARTY','Dự tiệc','Party','Trang phục nổi bật phù hợp buổi tiệc.','Expressive looks for parties and celebrations.'
    UNION ALL SELECT 'FORMAL_EVENT','Sự kiện trang trọng','Formal event','Trang phục lịch sự theo yêu cầu của sự kiện.','Refined outfits aligned with an event dress code.'
    UNION ALL SELECT 'DATE','Hẹn hò','Date','Trang phục giúp bạn thoải mái và tự tin khi gặp gỡ.','Comfortable looks that help you feel confident on a date.'
    UNION ALL SELECT 'TRAVEL','Du lịch','Travel','Dễ phối, dễ di chuyển và phù hợp lịch trình.','Easy-to-style outfits for your travel itinerary.'
    UNION ALL SELECT 'WEEKEND','Cuối tuần','Weekend','Trang phục thư giãn cho thời gian riêng.','Relaxed outfits for your downtime.'
    UNION ALL SELECT 'SPORT','Tập luyện','Workout','Trang phục năng động phù hợp hoạt động tập luyện.','Active outfits suited to your workout.'
    UNION ALL SELECT 'OUTDOOR','Hoạt động ngoài trời','Outdoor activities','Linh hoạt theo thời tiết và hoạt động ngoài trời.','Adaptable looks for outdoor plans and weather.'
    UNION ALL SELECT 'WEDDING','Dự đám cưới','Wedding guest','Lịch sự và phù hợp quy định trang phục của lễ cưới.','Considered outfits for the wedding dress code.'
) AS seed
JOIN occasions catalog ON catalog.code=seed.code
CROSS JOIN languages lang
WHERE lang.code IN ('vi','en')
ON DUPLICATE KEY UPDATE description = CASE
    WHEN occasion_translations.description IS NULL OR TRIM(occasion_translations.description)=''
    THEN CASE lang.code WHEN 'vi' THEN seed.description_vi WHEN 'en' THEN seed.description_en END
    ELSE occasion_translations.description END;

COMMIT;
