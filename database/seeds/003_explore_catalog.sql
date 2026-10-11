-- Idempotent catalog seed: exactly 40 Explore entries, preserving legacy styles and IDs.
-- Image URLs remain NULL until a licensed Cloudinary asset exists.
-- Run serially after 003_explore_catalog.sql; stop and ROLLBACK on any error.
USE fashion_ai;
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO style_groups(code,name_en,name_vi,sort_order) VALUES
('CLASSIC_ELEGANCE','Elegant & Classic','Thanh lịch và cổ điển',1),
('ROMANTIC_FEMININE','Feminine & Romantic','Nữ tính và lãng mạn',2),
('STREET_EDGE','Street & Alternative','Cá tính và đường phố',3),
('ARTISTIC_FREE','Free & Artistic','Tự do và nghệ thuật',4),
('MODERN_TRENDS','Modern & Trending','Hiện đại và xu hướng',5)
ON DUPLICATE KEY UPDATE id=id;
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'MINIMALIST',TRUE,1,'minimalist',g.id,TRUE FROM style_groups g WHERE g.code='CLASSIC_ELEGANCE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Tối giản','Đường nét gọn, ít chi tiết và các món đồ dễ phối giúp tạo tổng thể rõ ràng.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='MINIMALIST'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Minimalist','Clean lines and considered essentials with restrained detail.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='MINIMALIST'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'OLD_MONEY',TRUE,2,'old-money',g.id,TRUE FROM style_groups g WHERE g.code='CLASSIC_ELEGANCE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Cổ điển sang trọng','Phom cổ điển, bảng màu tiết chế và chất liệu có độ đứng vừa phải tạo vẻ chỉn chu.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='OLD_MONEY'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Old Money','Classic tailoring, restrained colours and understated accessories.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='OLD_MONEY'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'CLASSIC',TRUE,3,'classic',g.id,TRUE FROM style_groups g WHERE g.code='CLASSIC_ELEGANCE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Cổ điển','Sơ mi, blazer, quần thẳng và váy đơn giản được phối theo tỷ lệ cân đối.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='CLASSIC'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Classic','Enduring wardrobe shapes such as shirts, blazers and straight trousers.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='CLASSIC'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'QUIET_LUXURY',TRUE,4,'quiet-luxury',g.id,TRUE FROM style_groups g WHERE g.code='CLASSIC_ELEGANCE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Sang trọng tinh tế','Tập trung vào độ vừa vặn, cảm giác chất liệu và chi tiết hoàn thiện thay vì logo nổi bật.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='QUIET_LUXURY'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Quiet Luxury','An understated approach centred on fit, fabric and finish.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='QUIET_LUXURY'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'PARISIAN_CHIC',TRUE,5,'parisian-chic',g.id,TRUE FROM style_groups g WHERE g.code='CLASSIC_ELEGANCE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Thanh lịch Paris','Kết hợp trang phục cơ bản với điểm nhấn có chủ đích như khăn nhỏ hoặc giày thanh mảnh.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='PARISIAN_CHIC'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Parisian Chic','Relaxed basics with a deliberate finishing detail.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='PARISIAN_CHIC'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'PREPPY',TRUE,6,'preppy',g.id,TRUE FROM style_groups g WHERE g.code='CLASSIC_ELEGANCE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Học đường cổ điển','Áo polo, cardigan, kẻ sọc và chân váy xếp ly gợi cảm hứng trang phục học đường.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='PREPPY'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Preppy','Collegiate-inspired polos, knits, stripes and pleated skirts.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='PREPPY'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'BUSINESS_CASUAL',TRUE,7,'business-casual',g.id,TRUE FROM style_groups g WHERE g.code='CLASSIC_ELEGANCE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Công sở linh hoạt','Trang phục công sở bớt trang trọng, giữ sự gọn gàng và phù hợp quy định nơi làm việc.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='BUSINESS_CASUAL'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Business Casual','Polished workwear with a more relaxed level of formality.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='BUSINESS_CASUAL'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'SMART_CASUAL',TRUE,8,'smart-casual',g.id,TRUE FROM style_groups g WHERE g.code='CLASSIC_ELEGANCE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Thanh lịch thường ngày','Phối một món chỉn chu với trang phục thường ngày để cân bằng thoải mái và lịch sự.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='SMART_CASUAL'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Smart Casual','A balance of relaxed everyday pieces and polished accents.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='SMART_CASUAL'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'COQUETTE',TRUE,9,'coquette',g.id,TRUE FROM style_groups g WHERE g.code='ROMANTIC_FEMININE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Duyên dáng với nơ','Nơ, ren và chi tiết nhỏ tạo nét điệu đà; có thể tiết chế bằng đồ nền đơn giản.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='COQUETTE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Coquette','Bows, lace and delicate details balanced with simple foundations.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='COQUETTE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'BALLETCORE',TRUE,10,'balletcore',g.id,TRUE FROM style_groups g WHERE g.code='ROMANTIC_FEMININE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Cảm hứng ballet','Áo ôm mềm, cardigan quấn và giày bệt gợi trang phục tập múa trong cách mặc thường ngày.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='BALLETCORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Balletcore','Dance-inspired wrap knits, soft layers and ballet flats.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='BALLETCORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'COTTAGECORE',TRUE,11,'cottagecore',g.id,TRUE FROM style_groups g WHERE g.code='ROMANTIC_FEMININE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Đồng quê dịu dàng','Váy hoa nhỏ, tay bồng và chất liệu thoáng gợi không khí gần gũi với thiên nhiên.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='COTTAGECORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Cottagecore','Small florals, airy fabrics and pastoral-inspired silhouettes.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='COTTAGECORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'SOFT_GIRL',TRUE,12,'soft-girl',g.id,TRUE FROM style_groups g WHERE g.code='ROMANTIC_FEMININE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Ngọt ngào mềm mại','Sắc pastel, đồ dệt kim nhẹ và phụ kiện nhỏ tạo tổng thể mềm mại, tươi sáng.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='SOFT_GIRL'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Soft Girl','Pastels, light knits and playful delicate accessories.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='SOFT_GIRL'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'ROMANTIC',TRUE,13,'romantic',g.id,TRUE FROM style_groups g WHERE g.code='ROMANTIC_FEMININE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Lãng mạn','Ren, bèo nhẹ và chất liệu rủ tạo chuyển động mềm mà không cần nhiều chi tiết cùng lúc.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='ROMANTIC'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Romantic','Fluid fabrics, lace and gentle gathers create a romantic mood.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='ROMANTIC'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'PRINCESSCORE',TRUE,14,'princesscore',g.id,TRUE FROM style_groups g WHERE g.code='ROMANTIC_FEMININE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Cảm hứng công chúa','Váy có độ phồng, chi tiết xếp nếp và phụ kiện lấp lánh được điều chỉnh cho từng dịp.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='PRINCESSCORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Princesscore','Volume, gathered dresses and ornamental details adapted to the occasion.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='PRINCESSCORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'FAIRYCORE',TRUE,15,'fairycore',g.id,TRUE FROM style_groups g WHERE g.code='ROMANTIC_FEMININE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Cảm hứng tiên nữ','Lớp vải mỏng, đường gấu bất đối xứng và màu thiên nhiên tạo nét bay bổng.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='FAIRYCORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Fairycore','Sheer layering, irregular hems and nature-inspired colours.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='FAIRYCORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'CLEAN_GIRL',TRUE,16,'clean-girl',g.id,TRUE FROM style_groups g WHERE g.code='ROMANTIC_FEMININE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Gọn gàng tự nhiên','Các món đồ nền gọn, bảng màu nhẹ và phụ kiện tối giản tạo cảm giác chăm chút.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='CLEAN_GIRL'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Clean Girl','Streamlined basics and restrained accessories with a neat finish.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='CLEAN_GIRL'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'STREETWEAR',TRUE,17,'streetwear',g.id,TRUE FROM style_groups g WHERE g.code='STREET_EDGE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Đường phố','Phom rộng, sneaker và các lớp áo tạo cách phối linh hoạt cho sinh hoạt đô thị.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='STREETWEAR'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Streetwear','Relaxed silhouettes, sneakers and adaptable urban layers.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='STREETWEAR'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'GRUNGE',TRUE,18,'grunge',g.id,TRUE FROM style_groups g WHERE g.code='STREET_EDGE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Bụi bặm phóng khoáng','Flannel, denim và các lớp áo có vẻ cũ tự nhiên tạo nét bất quy tắc có chủ đích.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='GRUNGE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Grunge','Flannel, denim and deliberately informal layering.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='GRUNGE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'PUNK',TRUE,19,'punk',g.id,TRUE FROM style_groups g WHERE g.code='STREET_EDGE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Nổi loạn punk','Đồ da, kim loại và chi tiết tương phản mạnh được chọn theo mức độ cá tính mong muốn.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='PUNK'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Punk','Leather, metal accents and bold contrasts as expressive choices.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='PUNK'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'EDGY',TRUE,20,'edgy',g.id,TRUE FROM style_groups g WHERE g.code='STREET_EDGE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Sắc sảo cá tính','Đường cắt rõ, chất liệu tương phản và phụ kiện có cấu trúc tạo điểm nhấn sắc nét.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='EDGY'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Edgy','Sharp shapes, contrasting textures and structured accessories.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='EDGY'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'SKATER',TRUE,21,'skater',g.id,TRUE FROM style_groups g WHERE g.code='STREET_EDGE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Cảm hứng trượt ván','Áo thun, quần rộng và giày đế bám ưu tiên chuyển động và sự thoải mái.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='SKATER'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Skater','Movement-friendly tees, roomy trousers and practical flat shoes.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='SKATER'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'Y2K',TRUE,22,'y2k',g.id,TRUE FROM style_groups g WHERE g.code='STREET_EDGE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Cảm hứng đầu những năm 2000','Denim, phụ kiện nổi bật và các tỷ lệ áo ngắn gợi cách phối đồ đầu những năm 2000.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='Y2K'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Y2K','Denim, statement accessories and playful early-2000s proportions.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='Y2K'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'TECHWEAR',TRUE,23,'techwear',g.id,TRUE FROM style_groups g WHERE g.code='STREET_EDGE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Trang phục công năng','Nhiều túi, dây điều chỉnh và lớp áo bảo vệ thời tiết được chọn theo nhu cầu thực tế.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='TECHWEAR'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Techwear','Functional pockets, adjustable details and weather-conscious layers.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='TECHWEAR'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'GORPCORE',TRUE,24,'gorpcore',g.id,TRUE FROM style_groups g WHERE g.code='STREET_EDGE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Ngoài trời giữa phố','Áo khoác ngoài trời, quần tiện dụng và giày đi bộ được phối trong sinh hoạt thường ngày.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='GORPCORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Gorpcore','Outdoor-inspired jackets, practical trousers and walking footwear.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='GORPCORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'BOHEMIAN',TRUE,25,'bohemian',g.id,TRUE FROM style_groups g WHERE g.code='ARTISTIC_FREE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Du mục phóng khoáng','Vải rủ, họa tiết và phụ kiện thủ công tạo lớp lang thoải mái theo cá tính.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='BOHEMIAN'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Bohemian','Flowing fabrics, layered patterns and craft-inspired accessories.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='BOHEMIAN'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'ARTSY',TRUE,26,'artsy',g.id,TRUE FROM style_groups g WHERE g.code='ARTISTIC_FREE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Nghệ thuật sáng tạo','Màu, hình khối hoặc phụ kiện đặc biệt đóng vai trò điểm nhấn cho cách thể hiện cá nhân.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='ARTSY'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Artsy','Expressive colour, sculptural shapes and distinctive accessories.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='ARTSY'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'INDIE',TRUE,27,'indie',g.id,TRUE FROM style_groups g WHERE g.code='ARTISTIC_FREE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Độc lập cá tính','Trộn đồ cơ bản với món đồ cũ hoặc chi tiết riêng, ưu tiên gu cá nhân hơn sự đồng bộ.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='INDIE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Indie','Personal combinations of basics, secondhand finds and individual details.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='INDIE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'VINTAGE',TRUE,28,'vintage',g.id,TRUE FROM style_groups g WHERE g.code='ARTISTIC_FREE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Hoài cổ','Món đồ từ thời kỳ trước hoặc mang cảm hứng lịch sử được phối với đồ phù hợp hiện tại.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='VINTAGE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Vintage','Older garments or period-inspired pieces combined for present-day use.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='VINTAGE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'RETRO',TRUE,29,'retro',g.id,TRUE FROM style_groups g WHERE g.code='ARTISTIC_FREE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Tái hiện nét xưa','Màu sắc, đường cắt và họa tiết lấy cảm hứng quá khứ được thể hiện bằng đồ mới hoặc cũ.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='RETRO'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Retro','Past-inspired colours, silhouettes and patterns reinterpreted today.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='RETRO'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'ECLECTIC',TRUE,30,'eclectic',g.id,TRUE FROM style_groups g WHERE g.code='ARTISTIC_FREE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Pha trộn đa dạng','Phối nhiều nguồn cảm hứng bằng một điểm liên kết như màu, chất liệu hoặc tỷ lệ.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='ECLECTIC'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Eclectic','Mixed influences connected by colour, texture or proportion.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='ECLECTIC'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'HIPPIE',TRUE,31,'hippie',g.id,TRUE FROM style_groups g WHERE g.code='ARTISTIC_FREE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Tự do gần thiên nhiên','Phom thoải mái, chất liệu mộc và chi tiết thủ công gợi cảm hứng phản văn hóa.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='HIPPIE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Hippie','Relaxed shapes, natural textures and handcrafted details.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='HIPPIE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'MAXIMALIST',TRUE,32,'maximalist',g.id,TRUE FROM style_groups g WHERE g.code='ARTISTIC_FREE'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Tối đa sắc màu','Nhiều màu, họa tiết và phụ kiện được tổ chức để tổng thể có điểm nhấn rõ.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='MAXIMALIST'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Maximalist','Layered colours, patterns and accessories with deliberate focal points.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='MAXIMALIST'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'KOREAN_CASUAL',TRUE,33,'korean-casual',g.id,TRUE FROM style_groups g WHERE g.code='MODERN_TRENDS'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Thường ngày kiểu Hàn','Các lớp áo gọn, phom rộng vừa và bảng màu nhẹ tạo cách mặc thường ngày linh hoạt.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='KOREAN_CASUAL'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Korean Casual','Easy everyday layers, moderate volume and softly coordinated colours.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='KOREAN_CASUAL'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'ATHLEISURE',TRUE,34,'athleisure',g.id,TRUE FROM style_groups g WHERE g.code='MODERN_TRENDS'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Thể thao thường ngày','Đồ lấy cảm hứng vận động được phối để thuận tiện cho việc đi lại và sinh hoạt.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='ATHLEISURE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Athleisure','Activity-inspired clothing adapted for everyday comfort.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='ATHLEISURE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'SPORTY_CHIC',TRUE,35,'sporty-chic',g.id,TRUE FROM style_groups g WHERE g.code='MODERN_TRENDS'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Thể thao thanh lịch','Một món thể thao kết hợp blazer, túi gọn hoặc trang phục có phom rõ.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='SPORTY_CHIC'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Sporty Chic','Sportswear accents paired with polished, structured pieces.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='SPORTY_CHIC'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'NORMCORE',TRUE,36,'normcore',g.id,TRUE FROM style_groups g WHERE g.code='MODERN_TRENDS'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Cơ bản tự nhiên','Áo thun, denim và giày đơn giản tạo tổng thể bình dị với độ vừa vặn phù hợp.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='NORMCORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Normcore','Unassuming everyday basics worn with considered fit.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='NORMCORE'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'SCANDINAVIAN',TRUE,37,'scandinavian',g.id,TRUE FROM style_groups g WHERE g.code='MODERN_TRENDS'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Bắc Âu hiện đại','Phom thoải mái, chất liệu phù hợp thời tiết và màu sắc có chủ đích tạo nét thực dụng.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='SCANDINAVIAN'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Scandinavian','Practical, relaxed shapes with intentional colour and weather-aware fabrics.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='SCANDINAVIAN'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'ACUBI',TRUE,38,'acubi',g.id,TRUE FROM style_groups g WHERE g.code='MODERN_TRENDS'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Tối giản lớp lang Acubi','Áo ôm nhẹ, lớp vải mỏng và quần rộng tạo tương phản tỷ lệ trong bảng màu tiết chế.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='ACUBI'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Acubi','Light fitted layers and roomy bottoms in a restrained colour palette.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='ACUBI'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'DARK_ACADEMIA',TRUE,39,'dark-academia',g.id,TRUE FROM style_groups g WHERE g.code='MODERN_TRENDS'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Học thuật trầm','Tông tối, đồ dệt kim, blazer và giày cổ điển gợi không khí thư viện và học thuật.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='DARK_ACADEMIA'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Dark Academia','Dark tones, knits and tailored pieces with a scholarly mood.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='DARK_ACADEMIA'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO styles(code,is_active,sort_order,slug,group_id,is_explore_enabled)
SELECT 'LIGHT_ACADEMIA',TRUE,40,'light-academia',g.id,TRUE FROM style_groups g WHERE g.code='MODERN_TRENDS'
ON DUPLICATE KEY UPDATE slug=COALESCE(styles.slug,VALUES(slug)),group_id=VALUES(group_id),is_explore_enabled=TRUE;
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Học thuật sáng','Tông kem, be và chất liệu nhẹ phối cùng sơ mi, cardigan để tạo sắc thái học thuật sáng.' FROM styles s JOIN languages l ON l.code='vi' WHERE s.code='LIGHT_ACADEMIA'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
INSERT INTO style_translations(style_id,language_id,name,description)
SELECT s.id,l.id,'Light Academia','Creams, beiges and light layers with a scholarly-inspired mood.' FROM styles s JOIN languages l ON l.code='en' WHERE s.code='LIGHT_ACADEMIA'
ON DUPLICATE KEY UPDATE name=style_translations.name,description=COALESCE(style_translations.description,VALUES(description));
COMMIT;
