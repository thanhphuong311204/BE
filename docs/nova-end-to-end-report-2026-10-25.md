# NOVA AI Stylist — kiểm chứng ngày 25/10/2026

## Kết quả

Đã sửa gallery fallback bị Flutter loại bỏ, giữ vị trí đọc khi phản hồi đến, bổ sung sao chép HEX và xem ảnh phóng to. Backend 149 test và nhóm Flutter chatbox 39 test thành công. Chưa hoàn tất xác minh hội thoại Gemini thật thành công: các phép đo sinh nội dung đều timeout. Không đổi model, key, billing hoặc timeout.

## Phase 1–2: bằng chứng Gemini

Đọc mã backend, Flutter, migrations 008–010 và báo cáo `FE/docs/nova-timeout-library-report.md`. Luồng hiện tại: Flutter gửi requestId/conversationId → backend xác thực tài khoản, giữ reservation → đọc context và catalog MySQL → Gemini generateContent → parse/hydrate refs → lưu hai message và kết quả trong transaction → Flutter render. Replay request thành công đọc kết quả đã lưu; lỗi không tự retry hoặc lưu câu trả lời AI giả.

Model đang cấu hình `gemini-3.8-flash`, endpoint `/v1beta/models/{model}:generateContent`, xác thực qua header. Phép đọc metadata bằng key hiện có trả 200 và liệt kê `generateContent` trong supportedGenerationMethods. Điều này xác minh metadata/khả năng endpoint, không chứng minh quota sinh nội dung hoặc quyền free tier.

| Phép đo thật | HTTP upstream | Thời gian | Kết quả |
| --- | --- | --- | --- |
| Metadata model | 200 | 727,04 ms | Hỗ trợ generateContent |
| Chào hỏi tối giản, JSON | Chưa nhận được | 15.463,39 ms | ReadTimeout |
| Gợi ý outfit đi học, JSON | Chưa nhận được | 15.457,52 ms | ReadTimeout |
| Bảng màu pastel/HEX, không có ảnh đầu vào | Chưa nhận được | 15.418,87 ms | ReadTimeout |

Ba payload tổng hợp dùng cấu hình production tương ứng: maxOutputTokens 2048, temperature 0.7, responseMimeType application/json và thinkingLevel low. Không dùng lịch sử, nội dung source hoặc chỉ dẫn nội bộ. Đây là phép so sánh request trực tiếp bằng Python requests, không phải đo full-body deadline bằng Java hay toàn luồng Flutter. Requests timeout=(5,15) là connect/read timeout; không gán thời gian này thành giới hạn toàn request Java.

Một phép đo ban đầu riêng dùng câu chào thuần văn bản, maxOutputTokens 128, cũng ReadTimeout sau khoảng 15.274 ms; metadata trước đó 200/535 ms. Phép này không cùng generation config với ba dòng so sánh và chỉ ghi nhận như kiểm tra thăm dò.

Đã chứng minh: timeout tái hiện khi payload ngắn, không có history/MySQL. Không có response để parse. Chưa xác định lỗi ở provider, tuyến mạng cho POST hoặc xử lý generation; không kết luận context dài gây timeout và không coi timeout cục bộ là HTTP 504 từ Google. Không quan sát được HTTP 503/504 upstream trong các phép đo mới.

Theo [tài liệu thinking của Google](https://ai.google.dev/gemini-api/docs/generate-content/thinking), low là mức điều khiển reasoning để giảm độ trễ. Mã hiện có đã đặt low; phiên này giữ nguyên. Không thử đổi model hoặc tăng timeout vì người dùng yêu cầu xác nhận trước.

## Phase 3–5: sửa lỗi và UX

- Backend đã trả `library.blocks[].type=style_gallery` cho yêu cầu ảnh khi timeout/busy, nhưng Flutter chỉ giữ outfit_suggestions/color_palette. Đã cho phép gallery qua bộ lọc, giữ xác minh source nova_library và mã lỗi/status hiện có. Text fallback không được biến thành assistant message.
- Sheet trước đây cuộn xuống cuối khi nhận bất kỳ phản hồi nào. Bây giờ chỉ cuộn khi gửi chủ động, đổi phiên/màn lịch sử hoặc đang gần cuối. Việc đổi ID bản nháp thành ID hội thoại khi lưu phản hồi không tự kéo người dùng khỏi vị trí đọc.
- Mã HEX có thao tác sao chép đúng chuỗi và thông báo xác nhận. Ảnh thư viện có dialog phóng to bằng InteractiveViewer và nút đóng; dùng lại URL đã qua bộ lọc cùng loading/error renderer.
- Test backend cũ vẫn đòi `outfits[0].imageUrl` dù fallback ảnh đã dùng gallery. Đã chuyển sang kiểm tra type=style_gallery và images[0].imageUrl, giữ các kiểm tra không lưu AI giả, status, Retry-After, retry thủ công và replay.

Các cơ chế đã có được giữ: server hydrate ảnh từ refs MySQL/Cloudinary đã kiểm tra; giới hạn gallery 8; nhãn số ảnh thực tế khi thiếu ảnh; chống lặp URL đã lưu; lịch sử thuộc đúng tài khoản; Markdown chặn ảnh tự nhúng từ model; xác nhận xóa hội thoại. Không chỉnh JWT, mascot, bottom navigation, schema hay module khác.

## Tệp thay đổi trong phiên này

1. `BE/src/test/java/com/datn/fashion/AiVisualChatIntegrationTest.java`
2. `BE/scripts/probe_nova_provider.py`
3. `BE/docs/nova-end-to-end-report-2026-10-25.md`
4. `FE/lib/features/ai_stylist/controllers/ai_stylist_chat_controller.dart`
5. `FE/lib/features/ai_stylist/widgets/ai_stylist_chat_sheet.dart`
6. `FE/lib/features/ai_stylist/widgets/ai_message_content.dart`
7. `FE/test/ai_stylist_resilience_test.dart`
8. `FE/test/ai_stylist_visual_test.dart`

Working trees có nhiều thay đổi sẵn trước phiên; danh sách trên chỉ là phần phiên này chỉnh. Không commit hoặc hoàn nguyên thay đổi có sẵn.

## Phase 6: kiểm thử

| Kiểm tra | Kết quả/bằng chứng |
| --- | --- |
| Backend toàn bộ | 149 tests, 0 failures/errors/skipped; XML trong target/surefire-reports |
| Flutter nhóm chatbox | 39 tests pass; FE/output/nova-end-to-end-flutter-final.log |
| Analyzer sau chỉnh sửa cuối | No issues found; FE/output/nova-end-to-end-analyze-final.log |
| Flutter web build | Thành công; FE/output/nova-end-to-end-web-build.log |
| Backend package | Thành công; BE/output/nova-end-to-end-package.log |
| Khởi động MySQL/backend thật | Hikari pool và Hibernate validation thành công; Tomcat 8081, readiness ACCEPTING_TRAFFIC; request chưa đăng nhập trả 401 |
| Dừng backend thử | Đã dừng Java PID 26008, đối chiếu với log và cổng 8081; không dừng backend khác |
| Gallery sau lỗi | Regression test giữ gallery, requestId và restore bản nháp, không thêm AI message |
| Vị trí đọc | Widget test đọc tin cũ khi response đến giữ offset |
| Copy HEX/preview | Widget test kiểm tra Clipboard.setData đúng HEX, mở/đóng InteractiveViewer |
| Timeout/retry/history | Kiểm tra tự động provider có kiểm soát trong suite backend/Flutter; không suy diễn thành Gemini thật thành công |
| Gemini trực tiếp | Metadata 200, cả 3 generation ReadTimeout; BE/output/nova-end-to-end/provider.json |

Web build có cảnh báo dry-run Wasm và font CupertinoIcons có sẵn; build JavaScript vẫn thành công. Build được chạy trước bước chỉnh encoding nhãn tiếng Việt cuối; bản source cuối đã được analyzer và 39 test xác nhận.

Lượt Maven được PowerShell báo exit 1 do native stderr; XML Surefire mới ghi đủ 149 tests và không có failures/errors. Lượt package riêng trả exit 0. Harness kiểm tra readiness ban đầu nhận nhầm PID launcher Java; đã đối chiếu PID Java thực trong log/cổng, gọi HTTP thành công và dừng đúng tiến trình. `BE/output/nova-end-to-end-readiness.json` ghi kết quả kiểm tra cuối.

## Còn thiếu và chặn kiểm thử thật

- Chưa xác minh được Gemini generation thành công, hội thoại nối tiếp thật, retry phục hồi thật hoặc render Flutter sau response Gemini thật.
- Bộ duyệt tự động từ chối chạy script trích prompt hệ thống từ source gửi ra Google vì nguy cơ lộ nội dung nội bộ. Script bị chặn chưa được tạo/chạy. Đã chuyển sang payload tổng hợp công khai cho phép đo trực tiếp; câu hỏi xin phép end-to-end với prompt nội bộ đang chờ người dùng trả lời. Không chạy vòng qua quyết định này.
- Cloudinary thật được báo cáo phiên trước xác minh ba ảnh 200/giải mã được; phiên này chưa tải lại ảnh thật. Test UI xử lý ảnh dùng HTTP có kiểm soát, không chứng minh Cloudinary đang hoạt động.
- Chưa kiểm thử trực tiếp trên thiết bị Android/iOS. Thiết bị Flutter được phát hiện: Windows, Chrome, Edge. Không tuyên bố UX thiết bị hoặc pipeline end-to-end đã hoàn tất.
- Không xóa tài khoản, hội thoại, dữ liệu test hoặc đổi cấu hình để lấy kết quả thành công.

## Chạy lại

Từ `D:\DATN\BE`: `mvn test`, `mvn -DskipTests package`. Kiểm tra cổng chưa bị chiếm rồi chạy `java -jar target/fashion-backend-0.0.1-SNAPSHOT.jar --server.port=8081`; .env được đọc theo cấu hình có sẵn. Dùng Java binary thật trong JAVA_HOME khi quản lý tiến trình để tránh PID launcher khác PID JVM.

`python scripts/probe_nova_provider.py` gọi metadata và tối đa ba generation bằng prompt tổng hợp, không tự retry; dừng khi gặp 401/403/404/429/503. Script dùng key qua header, chỉ ghi HTTP/timing/errorType/methods/usage; không ghi key hoặc raw provider body. Mỗi lần chạy có thể tiêu quota hiện có.

Từ `D:\DATN\FE`: `flutter analyze`; `flutter test test/ai_stylist_controller_test.dart test/ai_stylist_history_test.dart test/ai_stylist_visual_test.dart test/ai_stylist_service_test.dart test/ai_stylist_resilience_test.dart test/ai_stylist_mascot_test.dart`; `flutter build web`. Để dùng app, chạy Flutter với API base URL theo cấu hình mạng hiện có và cổng backend thực tế; kiểm tra README/API configuration trước khi chọn thiết bị. Không dùng localhost trên điện thoại để trỏ vào máy tính.
