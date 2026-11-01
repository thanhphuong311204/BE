# Sửa lỗi mở cẩm nang phong cách — 12/10/2026

## Nguyên nhân chính xác

Hai JVM cùng chạy `com.datn.fashion.FashionApplication`, dùng classpath `D:\DATN\BE\target\classes`, Java 21, Spring Boot 3.5.6, artifact `fashion-backend:0.0.1-SNAPSHOT` nhưng đã nạp các phiên bản class khác nhau:

- PID 16488, cổng 8080: khởi động 14:47:04, DTO cũ chỉ nhận 11 thuộc tính JSON (10 phần cẩm nang + sources), không có `materials`/`silhouette`.
- PID 28012, cổng 8082: khởi động 15:40:01 với `--server.port=8082`, DTO mới nhận thêm hai phần trên và trả gallery.

Command line cả hai: Java JDK21, `-XX:TieredStopAtLevel=1 -cp @<spring-boot.argfile> com.datn.fashion.FashionApplication`; riêng PID 28012 có `--server.port=8082`. Các argfile đã được đối chiếu đúng classpath của BE trước khi dừng tiến trình.

APK đang cài trước khi sửa khớp SHA256 với APK build hiện tại; kernel chứa endpoint Android mặc định `http://10.0.2.2:8080`, không chứa override 8082. Flutter gọi `GET /api/explore/styles` và `GET /api/explore/styles/<slug>` với JWT, cụ thể `/dark-academia` và `/old-money`.

Tái hiện trên JVM 8080 cho full stack trace:

```text
org.springframework.dao.DataRetrievalFailureException: Invalid structured style guide
    at com.datn.fashion.repository.StyleGuideRepository.lambda$get$0(StyleGuideRepository.java:20)
...
Caused by: com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException:
Unrecognized field "materials" (class com.datn.fashion.dto.explore.StyleGuideResponse),
not marked as ignorable (11 known properties: "accessories", "mistakes", "recognition",
"sources", "overview", "palette", "occasions", "garments", "outfits", "suitability", "gettingStarted"])
```

Full stack trace nguyên bản, gồm request/controller/service/repository/Jackson, được lưu tại `output/style-library/full-old-backend-stacktrace.txt`. JFR xác nhận cùng root cause; không có SQLException trong lần tái hiện. Chẩn đoán tạm chỉ ghi stack trace ngoại lệ cẩm nang trên JVM cũ, không thay đổi HTTP 500, JWT hay dữ liệu, và đã kết thúc khi dừng JVM đó.

MySQL có `style_guides.content` kiểu JSON và đủ schema `style_outfit_images` của migration 006. Dữ liệu mới hợp lệ; cùng database, backend 8082 trả 200 và đủ 12 phần. Không có entity mapping lỗi: guide/gallery được đọc bằng JdbcTemplate. Việc compile lại class trên đĩa không cập nhật các class JVM 8080 đã nạp.

## Cách sửa

Dừng đúng hai JVM của BE sau khi xác minh command line và classpath; khởi động code hiện tại bằng:

```powershell
cd D:\DATN\BE
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080
```

Backend mới PID 8908 chạy cổng 8080; cổng 8082 đã được giải phóng. Flutter dùng cấu hình mặc định Android 8080. Không thay đổi hoặc xóa database, không chạy seed/migration, không thay URL, không xóa ảnh Cloudinary, không bỏ trường JSON hoặc trả guide giả để che lỗi.

Chỉnh integration test dùng `ApiConfig.baseUrl` thay hardcode 8082. Script `check_live_library.py` mặc định 8080 và hỗ trợ `--port` khi cần kiểm tra môi trường khác. DTO mới đã có trong source trước lượt sửa này; nguyên nhân nằm ở phiên bản JVM đang chạy, không cần sửa DTO nữa.

## HTTP trước/sau

| Endpoint (JWT hợp lệ) | 8080 cũ | 8082 trước sửa | 8080 mới |
| --- | --- | --- | --- |
| GET /api/explore/styles | 200, 40 phong cách | 200 | 200, 40 phong cách |
| GET /api/explore/styles/dark-academia | 500 | 200 | 200 |
| GET /api/explore/styles/old-money | 500 | 200 | 200 |

`/actuator/health` không phải phép kiểm tra health hợp lệ cho repository hiện tại: pom.xml không có Actuator. Không JWT nhận 401 từ SecurityFilterChain; có JWT nhận 404 vì endpoint chưa tồn tại. Không mở quyền truy cập hoặc thêm Actuator trong lượt sửa lỗi này.

Kiểm tra API tất cả 40 chi tiết trên 8080: đủ 40 guide, 40 URL cover, 201 công thức; nội dung, image_url và content_version bằng chính snapshot trước khi sửa.

## File cập nhật trong lượt sửa lỗi

- BE `scripts/check_live_library.py`: cổng mặc định 8080, tùy chọn --port.
- FE `integration_test/style_library_live_test.dart`: dùng ApiConfig cùng ứng dụng.
- `docs/style-library.md` và báo cáo này: thống nhất hướng dẫn vận hành và ghi nguyên nhân.

Log/chẩn đoán nằm trong thư mục `output/` đã gitignore. Không chứa JWT hoặc credentials trong các báo cáo kết quả.

## Kết quả kiểm thử sau sửa

- `mvn test`: 105 tests, không lỗi/thất bại.
- `flutter analyze`: không có issues.
- `flutter test test/explore_detail_test.dart test/style_library_test.dart`: 48 tests đạt.
- Android Emulator `emulator-5554`: integration test dùng ApiConfig mặc định 8080 đạt; mở đủ 40 trang, trong đó có Dark Academia và Old Money, kiểm tra đủ 12 ExpansionTile và tải/giải mã mỗi cover Cloudinary thật.
- HTTP của cả 40 chi tiết là 200; không sửa content/content_version/image_url so với snapshot trước sửa.
- Cài lại ứng dụng Flutter thường với cấu hình mặc định sau integration test; JWT kiểm thử tạm đã được xóa khỏi file cấu hình.
