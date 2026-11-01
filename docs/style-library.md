# NOVA ATELIER — thư viện 40 phong cách

## Dữ liệu và tài sản thật (12/10/2026)

- MySQL `fashion_ai`: 40 phong cách đang hoạt động, thuộc 5 nhóm.
- 40 cẩm nang có đủ 12 phần và tối thiểu 5 công thức mỗi phong cách; tổng 201 công thức vì Old Money giữ nguyên 6 công thức cũ.
- 40 ảnh cover riêng, được tạo bằng công cụ OpenAI image_gen thực tế, kiểm tra từng ảnh, kích thước 1086×1448 (3:4). Không dùng ảnh Internet.
- 40 URL Cloudinary gốc đã tải, kiểm tra HTTP 200 và giải mã ảnh; 40 URL delivery tối ưu 720×960 cũng đã kiểm tra. Tổng dung lượng 40 ảnh delivery khoảng 2,63 MB.
- `database/content/style-cover-assets.json` ghi URL, SHA256, file nguồn và nguồn gốc cho từng cover. File PNG nguồn tại `assets/style-covers/`; prompt tại `database/content/style-cover-prompts.json`.
- Chưa có ảnh minh họa riêng cho 201 công thức. `database/content/style-outfit-upload.todo.json` liệt kê đầy đủ slug, outfitKey, trang phục, prompt và caption cần bổ sung. Trường file/provenance trống là trạng thái chưa có tài sản, không phải URL giả.

## Triển khai trên database khác

Không sửa `.env` đã có. Các công cụ đọc DB/Cloudinary credentials nội bộ, không in mật khẩu, API secret hoặc JWT.

```powershell
cd D:\DATN\BE
python -m pip install -r scripts/requirements-style-library.txt
python scripts/style_library.py audit --output output/style-library/before.json
```

Kiểm tra bảng `style_guides` và catalog hiện có trước; catalog phải khớp đúng 40 slug. Chạy migration `database/migrations/006_style_outfit_images.sql` sau khi kiểm tra bảng gallery chưa tồn tại hoặc có schema tương thích. Đây là migration cộng thêm, không sửa Auth/Profile/Style Onboarding.

```powershell
python scripts/style_library.py seed-guides --input database/content/style-guides-40.json
python scripts/style_library.py seed-guides --input database/content/style-guides-40.json --apply
python scripts/style_library.py upload --input database/content/style-cover-upload.json
python scripts/style_library.py upload --input database/content/style-cover-upload.json --apply
python scripts/style_library.py verify --output output/style-library/verified-covers.json
```

Mặc định seed/upload chỉ kiểm tra, cần `--apply` để ghi. Seed sao lưu trước vào `output/style-library/`, chỉ thêm guide chưa có hoặc key bị thiếu; giữ mọi section đã có kể cả section rỗng do người dùng chỉnh sửa. Cover có URL được giữ nguyên. Script upload không tự thay cover cũ; muốn thay ảnh cần người quản lý duyệt URL cụ thể.

## Upload outfit hàng loạt

Sao chép file TODO thành manifest riêng. Chỉ giữ những mục có ảnh thực tế và điền:

```json
{
  "images": [
    {
      "slug": "minimalist",
      "kind": "outfit",
      "outfitKey": "minimalist-1",
      "file": "D:/your-owned-assets/minimalist-1.jpg",
      "caption": "Ảnh minh họa công thức đã được kiểm tra",
      "provenance": "Mô tả nguồn gốc và quyền sử dụng thực tế"
    }
  ]
}
```

Chạy `upload --input <manifest>` để kiểm tra, rồi thêm `--apply`. Cho phép nhiều ảnh cùng outfitKey. Kiểm tra file JPEG/PNG/WebP, tối đa 5 MB, chiều lớn nhất 4096, cover đúng 3:4, không trùng nội dung trong batch, slug/key phải tồn tại. Gallery được lưu độc lập với JSON guide, tránh ghi đè nội dung người dùng. Cùng hash đã gắn vào một phong cách được bỏ qua khi chạy lại. Cloudinary upload signed, overwrite=false và ID ổn định theo hash cho batch mới; retry có giới hạn, giữ kiểm tra TLS.

API chi tiết trả `style`, `guide`, `images`; mỗi ảnh có `outfitKey`, `imageUrl`, `caption`. Flutter nối gallery theo key, chỉ render URL Cloudinary HTTPS hợp lệ. Cover không được giả làm ảnh outfit. Khi không có ảnh outfit, nội dung vẫn hiển thị và ghi rõ trạng thái.

## Giao diện và kiểm thử

- Dropdown 5 nhóm + Tất cả lấy từ API, tên nhóm đang chọn, lọc kết hợp tên tiếng Anh/Việt và số lượng kết quả.
- Cover card/hero 3:4, ảnh delivery Cloudinary tối ưu, loading và xử lý URL thiếu/ảnh hỏng.
- 12 mục phân cấp bằng accordion để đọc trên điện thoại; bảng màu trực quan, công thức có trang phục, hoàn cảnh, giải thích và gallery khi có tài sản hợp lệ.
- `mvn test`: kiểm tra catalog, 40 guide qua database/API, gallery riêng từng style và hồi quy Auth/Profile/Avatar/JWT/Onboarding.
- `flutter analyze`, `flutter test`: fixture được lấy từ API MySQL thật, 40 trang chi tiết, dropdown kết hợp tìm kiếm ở 320×568, 430×932, 568×320 với chữ 1,3×; giữ tương thích guide cũ 10 phần.
- `integration_test/style_library_live_test.dart`: Android Emulator, API thật dùng cùng ApiConfig với ứng dụng (mặc định 10.0.2.2:8080), đọc JWT kiểm thử qua TEST_ACCESS_TOKEN. Cần token hợp lệ; không lưu token trong repository. `scripts/check_live_library.py` mặc định cổng 8080, có tùy chọn --port; không ghi token vào báo cáo.

Kết quả đã chạy: 105 backend tests đạt; 3 tests công cụ seed/upload đạt; `flutter analyze` không có lỗi; 133 Flutter tests đạt; integration test Android đạt, tải/giải mã 40 cover thực tế và mở 40 trang đủ 12 phần. Sau lượt sửa lỗi JVM cũ, backend được thống nhất về cổng 8080; xem `docs/backend-style-library-fix.md`. Cần restart backend sau khi cập nhật DTO, vì compile trên đĩa không cập nhật class đã nạp trong JVM đang chạy.

### Kiểm tra thủ công trên emulator

1. Khởi động backend mới đã compile trên cổng 8080, chạy Flutter với cấu hình mặc định. Nếu chủ động đổi cổng, truyền API_BASE_URL tương ứng; chỉ chạy một backend cho môi trường này.
2. Đăng nhập, mở Khám phá, xác nhận 40/40, mở dropdown đủ 6 lựa chọn.
3. Chọn từng nhóm, tìm tên có/không có trong nhóm, xóa tìm kiếm rồi chọn Tất cả; đối chiếu số kết quả.
4. Mở từng phong cách, kiểm tra cover riêng, đủ 12 phần, palette và ít nhất 5 công thức. Old Money có 6 công thức.
5. Xoay màn hình, tăng cỡ chữ, mở bàn phím tìm kiếm, cuộn hết danh sách và cẩm nang; kiểm tra không overflow.
6. Tắt mạng rồi mở ảnh chưa cache để xem lỗi ảnh; bật mạng và vào lại. Không coi ảnh đã cache là bằng chứng kết nối mới.
7. Khi bổ sung ảnh outfit bằng manifest, vào lại chi tiết để kiểm tra ảnh và caption đúng công thức, không xuất hiện ở phong cách khác.

Không triển khai chức năng AI, Tủ đồ hoặc Settings trong nhiệm vụ này.

## File thay đổi

BE: `.gitignore`; 3 DTO ExploreStyleDetailResponse/StyleGuideResponse/StyleImageResponse; StyleGuideRepository; ExploreCatalogService; ExploreCatalogIntegrationTest; schema-test.sql; migration 006; toàn bộ dữ liệu `database/content/`; 40 PNG `assets/style-covers/`; scripts style_library/build_style_guides/check_live_library/test_style_library và requirements; tài liệu này.

FE: `lib/features/home/models/style_guide.dart`; screens explore_screen/explore_detail_screen; services explore_service; widgets discovery_style_card/explore_style_cover/style_guide_sections; `pubspec.yaml`/lock (integration_test SDK); `test/home_discovery_test.dart`; `test/style_library_test.dart`; fixture style_library_api.json; integration_test/style_library_live_test.dart.
