# Đề xuất bổ sung danh mục — chưa được thực thi

File SQL: `002_editorial_catalog_expansion.sql`.

Đối chiếu với DDL trong dự án và xác nhận database của người dùng: ID BIGINT UNSIGNED, UNIQUE(code), khóa chính kép (catalog_id, language_id), khóa ngoại tới danh mục/languages và engine InnoDB. Không có thông tin đăng nhập MySQL trong phiên này nên chưa đọc lại dữ liệu/ràng buộc trên server thật. Số lượng nền đã được người dùng xác nhận: 9 styles, 8 colors, 6 occasions.

SQL chỉ INSERT vào bảng hiện có. ID được tìm bằng code, không cố định ID. Chạy lại không tạo bản ghi trùng nhờ UNIQUE/PRIMARY KEY. Giữ tên, HEX, trạng thái active và thứ tự của bản ghi đã tồn tại; chỉ bổ sung mô tả dịch đang NULL/rỗng. Thêm bản dịch vi/en còn thiếu. Không ghi users, user_profiles hoặc sở thích. Không ALTER/CREATE/DROP.

Mục tiêu sau duyệt: 18 styles, 24 colors, 12 occasions; 36/48/24 bản dịch nếu dữ liệu nền tương ứng các code đã xác nhận. Các mục đã inactive vẫn giữ inactive nên số lựa chọn API có thể ít hơn.

## Phong cách

| Code | Tiếng Việt | English |
|---|---|---|
| MINIMALIST | Tối giản | Minimalist |
| ELEGANT | Thanh lịch | Elegant |
| CASUAL | Thường ngày | Casual |
| STREETWEAR | Đường phố | Streetwear |
| ROMANTIC | Lãng mạn | Romantic |
| SPORTY | Thể thao | Sporty |
| OLD_MONEY | Cổ điển sang trọng | Old money |
| VINTAGE | Hoài cổ | Vintage |
| FEMININE | Nữ tính | Feminine |
| BOHEMIAN | Bohemian phóng khoáng | Bohemian |
| PREPPY | Preppy học đường | Preppy |
| CLASSIC | Cổ điển | Classic |
| CHIC | Chic hiện đại | Chic |
| MONOCHROME | Đơn sắc | Monochrome |
| SMART_CASUAL | Lịch sự thoải mái | Smart casual |
| EDGY | Cá tính | Edgy |
| COQUETTE | Coquette duyên dáng | Coquette |
| RESORT | Nghỉ dưỡng | Resort |

## Màu

HEX là giá trị hiển thị swatch; màu vải ngoài thực tế có thể khác theo chất liệu và ánh sáng. Tám giá trị nền được giữ nguyên khi chạy lại.

| Code | Tiếng Việt | English | HEX |
|---|---|---|---|
| BLACK | Đen | Black | #191816 |
| WHITE | Trắng | White | #FFFFFF |
| IVORY | Trắng ngà | Ivory | #FAF8F4 |
| BEIGE | Be | Beige | #D8C3A5 |
| BROWN | Nâu | Brown | #8B684F |
| BLUE | Xanh dương | Blue | #547AA5 |
| PINK | Hồng | Pink | #DCA6B2 |
| GREEN | Xanh lá | Green | #728C69 |
| NAVY | Xanh navy | Navy | #000080 |
| CHARCOAL | Xám than | Charcoal | #36454F |
| LIGHT_GRAY | Xám nhạt | Light gray | #D3D3D3 |
| SILVER | Bạc | Silver | #C0C0C0 |
| CREAM | Kem | Cream | #FFFDD0 |
| CAMEL | Nâu camel | Camel | #C19A6B |
| CHOCOLATE | Nâu chocolate | Chocolate | #7B3F00 |
| TAUPE | Nâu xám | Taupe | #8B8589 |
| DUSTY_ROSE | Hồng đất | Dusty rose | #C08081 |
| BLUSH | Hồng phấn | Blush | #F4C2C2 |
| BURGUNDY | Đỏ rượu vang | Burgundy | #800020 |
| RED | Đỏ | Red | #FF0000 |
| ORANGE | Cam | Orange | #FFA500 |
| MUSTARD | Vàng mù tạt | Mustard | #FFDB58 |
| LAVENDER | Tím lavender | Lavender | #E6E6FA |
| OLIVE | Xanh olive | Olive | #808000 |

## Hoàn cảnh

| Code | Tiếng Việt | English |
|---|---|---|
| SCHOOL | Đi học | School |
| WORK | Đi làm | Work |
| DAILY | Sinh hoạt thường ngày | Everyday |
| OUTING | Đi chơi | Going out |
| PARTY | Dự tiệc | Party |
| FORMAL_EVENT | Sự kiện trang trọng | Formal event |
| DATE | Hẹn hò | Date |
| TRAVEL | Du lịch | Travel |
| WEEKEND | Cuối tuần | Weekend |
| SPORT | Tập luyện | Workout |
| OUTDOOR | Hoạt động ngoài trời | Outdoor activities |
| WEDDING | Dự đám cưới | Wedding guest |

SQL có START TRANSACTION/COMMIT. Chỉ thực thi sau phê duyệt; chạy tuần tự trên một connection, dừng và ROLLBACK khi gặp lỗi, không tiếp tục đến COMMIT. Chưa chạy seed mới hoặc chạy lại migration/seed cũ.

Ảnh và mô tả fallback trong backend chỉ là metadata trình bày cho code đã tồn tại trong database; chúng không tạo lựa chọn mới. Nếu database có mô tả, API ưu tiên mô tả đó.
