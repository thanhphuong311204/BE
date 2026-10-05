# Fashion backend — Auth & Profile

Java 21, Spring Boot 3.5.6, Spring Security/JPA, MySQL. Backend: D:\DATN\BE; Flutter: D:\DATN\FE.

## Database source of truth

The supplied existing fashion_ai schema is authoritative:
- users stores account/authentication only: id, email, password_hash, status, verification/login/soft-delete timestamps, creation/update timestamps.
- user_profiles stores personal data: user_id shared PK/FK, full_name(150), avatar_url(1000), date_of_birth, gender, bio(500), timestamps.
- phone_number(20) is nullable and UNIQUE after the approved migration.
- preferred_language_id exists in users but is not mapped because these services do not use it. Its existing foreign key and all other tables are untouched.
- No auth_provider, provider_id, avatar_public_id, full_name or avatar_url mapping in users.

Run database/migrations/001_add_phone_number.sql once in MySQL Workbench on fashion_ai before starting this version.
This file contains ONLY the approved ALTER adding phone_number and its unique constraint.
It is not auto-executed. No live database commands or data updates were performed during the refactor.
Do not rerun it after the column/constraint already exists.

Runtime application.yml retains ddl-auto: validate. Never enable update/create for the existing database.
No additional migration is required against the supplied schema after phone_number is added.
Live MySQL startup remains unverified because connection credentials were not provided.

## Auth / Profile behavior

Register validates fullName/email/phone/password/confirmation, normalizes identifiers, checks duplicates,
hashes with BCrypt, then saves User and UserProfile inside one transaction. Profile failure rolls back the account.
Login normalizes email or phone, requires ACTIVE and deleted_at IS NULL, verifies BCrypt,
updates last_login_at and issues JWT. INACTIVE/SUSPENDED/deleted accounts receive the same generic credentials error.

GET profile aggregates User + UserProfile. Name/avatar updates target user_profiles; phone updates target users.
DOB/gender/bio are mapped, but adding their editing endpoints/UI is outside this refactor.
Existing request and response contracts remain unchanged:
- register: fullName, email, phoneNumber, password, confirmPassword -> profile (201).
- login: identifier, password -> token, user (200).
- GET /api/profile: Bearer JWT -> profile.
- PUT /api/profile: fullName, phoneNumber -> profile.
- POST /api/profile/avatar: multipart field file -> profile.
- profile: id, fullName, email, phoneNumber, avatarUrl; password/hash never exposed.

Old accounts may have a NULL phone. Their response uses an empty string to preserve the Flutter String contract,
while stored data stays NULL. Missing user_profiles returns 404 PROFILE_NOT_FOUND; the backend does not
silently create or modify legacy data. No Flutter changes were necessary; its existing name limit remains 100,
which is stricter than the backend/database limit of 150.

Email profile field remains read-only. Unknown update fields are rejected; account ID comes only from validated JWT.
JWT remains stateless; logout deletes the local token, without server-side revocation.
No Google login, refresh-token implementation, AI or commerce features were added.

## 2. Chạy BE (PowerShell thường)

Java Maven hiện dùng JDK 21. Mở Terminal tại D:\DATN\BE.
Thay YOUR_DATABASE và YOUR_USERNAME bằng thông tin thật:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/YOUR_DATABASE?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8'
$env:DB_USERNAME = 'YOUR_USERNAME'
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'MySQL password' -AsSecureString)).Password
$jwtBytes = New-Object byte[] 48
$jwtRandom = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtRandom.GetBytes($jwtBytes)
$jwtRandom.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$env:JWT_TTL_SECONDS = '3600'
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5000'
mvn spring-boot:run
```

JWT_SECRET phải có ít nhất 32 byte UTF-8. Ví dụ trên sinh key ngẫu nhiên, không in key.
Giữ secret ổn định trong cấu hình riêng nếu muốn token còn hợp lệ qua các lần restart.
Không dùng key trong application-test.yml ở runtime thật.

.env.example chỉ là tài liệu các biến; Spring KHÔNG tự đọc .env.
Đặt biến môi trường trong Terminal/IDE; .gitignore bỏ qua .env, target và cấu hình local.
URL localhost có useSSL=false chỉ cho phát triển tại máy. Dùng TLS/HTTPS khi triển khai.

## 3. Cloudinary

Đặt biến môi trường trong cùng Terminal chạy BE, trước mvn spring-boot:run:

```powershell
$env:CLOUDINARY_CLOUD_NAME = 'YOUR_CLOUD_NAME'
$env:CLOUDINARY_API_KEY = 'YOUR_API_KEY'
$env:CLOUDINARY_API_SECRET = [System.Net.NetworkCredential]::new('', (Read-Host 'Cloudinary API secret' -AsSecureString)).Password
```

## Avatar persistence

JPEG/PNG contents are decoded and validated, maximum 5 MB and 4096 x 4096 pixels.
Only Cloudinary HTTPS secure_url is stored in user_profiles.avatar_url (max1000).
The new public ID is held temporarily in memory for cleanup if the transaction rolls back.
Old avatar assets are not automatically deleted because the supplied schema has no persisted public ID.
No IDs are guessed from URLs and no extra database column is introduced.
Provider credentials and real uploads remain unverified; integration tests mock Cloudinary.

## Tests

Run `mvn -B test`. Result: 21 tests passed, 0 failures/errors/skipped.
The test profile initializes schema-test.sql on an isolated H2 database and uses Hibernate validate.
It does not generate schema from entities or connect to production MySQL.
Coverage includes registration in both tables, rollback, duplicates, identifier normalization,
email/phone login, wrong passwords, inactive/suspended/deleted rejection, last_login_at,
JWT authorization, profile aggregation/correct-table update, legacy NULL phone, missing profile,
name length, avatar validation/persistence/provider failure and oversized URL rollback cleanup.

H2 cannot fully reproduce MySQL unsigned BIGINT range, collation, engine locking or timestamp ON UPDATE behavior.
Live MySQL validation and real Cloudinary uploads are separate deployment checks.

Exact CREATED/MODIFIED/DELETED files and responsibilities: FILES.md.
