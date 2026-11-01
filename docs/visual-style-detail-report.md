# NOVA ATELIER — Visual style detail (2026-10-12)

## Giao diện
- Hero cover Cloudinary 3:4, tên/danh mục và mô tả giới hạn 3 dòng.
- Bảng màu có tên; nhấn để xem mã màu và cách kết hợp.
- Outfit có ảnh: lưới 2 cột, tỷ lệ 3:4, tên và mô tả tối đa 2 dòng. Khi chỉ có 1 ảnh hoặc chữ phóng lớn, dùng 1 cột để tránh khoảng trống.
- Outfit thiếu ảnh: danh sách gọn; nhấn xem đủ món đồ, hoàn cảnh và hướng dẫn. Không lấy cover thay ảnh outfit.
- 4 điểm nhận diện ngắn; nhấn xem nội dung đầy đủ.
- Trang phục/phụ kiện: tên ngắn, nhấn mở thông tin. Chưa có ảnh riêng của từng loại item.
- Cẩm nang được chuyển sang màn hình đọc, chọn 1 trong đủ 12 mục. Toàn bộ nội dung, công thức, nguồn tham khảo được giữ nguyên.

## File thay đổi trong lượt này
Frontend D:/DATN/FE:
- lib/features/home/screens/explore_detail_screen.dart
- lib/features/home/widgets/style_guide_sections.dart
- lib/features/home/widgets/style_outfit_card.dart (mới)
- lib/features/home/screens/outfit_detail_screen.dart (mới)
- lib/features/home/screens/style_guide_reader_screen.dart (mới)
- test/explore_detail_test.dart
- test/style_library_test.dart
- test/visual_style_detail_test.dart (mới)
- test/fixtures/style_library_api.json (đồng bộ API thật sau upload)
- integration_test/style_library_live_test.dart

Backend D:/DATN/BE:
- assets/style-outfits/*.png: 5 ảnh gốc thực tế, 1086×1448.
- database/content/visual-outfit-upload.json: manifest mapping theo slug + outfitKey.
- database/content/visual-outfit-prompts.json: prompt riêng đã dùng với builtin imagegen.
- database/content/missing-outfit-images.json: danh sách chi tiết 196 ảnh thiếu, gồm món đồ.
- docs/visual-style-detail-report.md
Không thay đổi Java, schema, Auth, Profile, AI Studio, Settings hoặc Chatbot trong lượt này.

## Ảnh và dữ liệu
Đã kiểm tra API/MySQL trước khi sửa. Trước: 40 cover, 201 công thức, 0 ảnh outfit.
Sau: 40 cover giữ nguyên, 201 công thức giữ nguyên, 5 ảnh outfit mới; còn thiếu 196.
Năm ảnh đều có người mẫu nữ trưởng thành; đã kiểm tra trực quan trang phục, màu sắc, giày/túi và tỷ lệ 3:4. Không tải ảnh Internet.
- Old Money: linen-campus.
- Dark Academia: dark-academia-1.
- Coquette: coquette-1.
- Streetwear: streetwear-1.
- Minimalist: minimalist-1.
Đã upload Cloudinary và INSERT vào style_outfit_images bằng uploader hiện có. Không seed lại, không UPDATE guide, không xóa/ghi đè cover.
Cả 5 URL trả HTTP 200 và giải mã thành công trên Android. API xác nhận đủ 40 guide, 12 mục, 201 công thức, 5 gallery images. URL và HTTP status trong output/visual-detail/image-audit-after.json.

## Kiểm thử
- flutter analyze: No issues found.
- Kiểm tra palette/garment bottom sheet, đóng sheet và navigation ở 3 kích thước: 4 tests pass.
- Toàn bộ Flutter suite sau sửa cuối: 148 tests pass, gồm mapping, bố cục 320px/chữ lớn, thu gọn ảnh lỗi và viewport Android tạm thời bằng 0.
- 40 widget tests mở trang chi tiết và xác nhận từng đoạn nội dung gốc trong đủ 12 mục qua reader.
- Emulator emulator-5554: integration test thật MySQL → API → Flutter pass; giải mã đủ 40 cover và 5 outfit; kiểm tra 40 trang; thao tác mở outfit, đọc mục Chất liệu và back trên Old Money, Dark Academia, Coquette, Streetwear, Minimalist.
- mvn -Dtest=ExploreCatalogIntegrationTest test: 7 tests pass.
- Python uploader unit tests: 3 pass.

Đã kiểm tra trực tiếp cold start trên emulator: xử lý viewport tạm thời bằng 0 trước khi tính lưới, tránh negative width.

## Giới hạn
Thư viện chưa thể đạt 75–80% trải nghiệm bằng ảnh outfit trên toàn bộ 40 phong cách khi còn thiếu 196 ảnh. Giao diện hỗ trợ thêm ảnh qua mapping hiện tại mà không cần thay code Flutter. Chưa có ảnh tách riêng từng loại trang phục/phụ kiện; không tạo ảnh giả để lấp chỗ trống.

## Kiểm tra thủ công
Mở Khám phá → Old Money/Dark Academia/Coquette/Streetwear/Minimalist. Cuộn hero → bảng màu → outfit; nhấn ảnh xem đầy đủ món đồ, hoàn cảnh, cách phối; back; nhấn màu và trang phục; mở Đọc thêm và chọn từng mục. Thử màn hình nhỏ, chữ lớn và mất mạng. Các công thức chưa có ảnh vẫn mở đầy đủ nội dung và hiển thị trạng thái bổ sung ảnh.