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

Ứng dụng tự đọc .env tại thư mục gốc dự án. Sao chép .env.example thành .env, điền cấu hình local rồi chạy mvn spring-boot:run. Biến môi trường Terminal/IDE được ưu tiên hơn .env.
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

Run `mvn -B test`. Result: 31 tests passed, 0 failures/errors/skipped (including Style Onboarding).
The test profile initializes schema-test.sql on an isolated H2 database and uses Hibernate validate.
It does not generate schema from entities or connect to production MySQL.
Coverage includes registration in both tables, rollback, duplicates, identifier normalization,
email/phone login, wrong passwords, inactive/suspended/deleted rejection, last_login_at,
JWT authorization, profile aggregation/correct-table update, legacy NULL phone, missing profile,
name length, avatar validation/persistence/provider failure and oversized URL rollback cleanup.

H2 cannot fully reproduce MySQL unsigned BIGINT range, collation, engine locking or timestamp ON UPDATE behavior.
Live MySQL validation and real Cloudinary uploads are separate deployment checks.

Exact CREATED/MODIFIED/DELETED files and responsibilities: FILES.md.

## Style Onboarding

The existing, manually applied 002_style_preferences.sql and catalog seed are prerequisites.
Do not run either SQL file again as part of starting the application. Hibernate remains validate.

All endpoints require the existing JWT (Authorization: Bearer <token>):
- GET /api/style-preferences/options?language=vi (vi or en): object containing styles, colors and occasions.
  Each option contains id, code, name, description, hexCode and illustrationIndex (nullable as appropriate).
  Returns active entries ordered by sort_order/id; missing or inactive-language translations fall back to code.
- GET /api/style-preferences: status, styleIds, colorIds, occasionIds, completedAt, skippedAt.
  No row returns NOT_STARTED and empty selections; the read does not create a row.
- PUT /api/style-preferences: {"styleIds":[1],"colorIds":[2],"occasionIds":[3]}.
  IDs are obtained from options, not fixed seed IDs. Replaces selections and marks COMPLETED.
- POST /api/style-preferences/skip: persists SKIPPED, idempotently; never downgrades COMPLETED.

Validation: 1–3 distinct active styles, 1–6 distinct active colors and 1–5 distinct active occasions,
positive non-null IDs; rejects unknown request properties including userId.
User identity comes only from the JWT subject. New endpoints reject missing, inactive,
suspended or deleted accounts. Save/skip serialize on the existing account row;
save also locks selected catalog rows during validation. State and all selection rows
are persisted in one transaction. completedAt is refreshed on successful save; skippedAt
is retained as history after later completion. Timestamp values use seconds to match MySQL TIMESTAMP.

34 backend tests pass: 22 Auth/Profile plus 12 onboarding integration tests.
Onboarding fixtures initialize only H2; no MySQL migration, seed or live account is executed by tests.
Coverage: JWT, translated/active/sorted catalogs, fallback, empty/missing/duplicate/oversized IDs,
invalid/inactive IDs, isolated users, skip/repeat/complete/update, account status and transaction rollback.
H2 tests do not replace an end-to-end check against the user's running MySQL instance.

## Editorial presentation upgrade

Profile responses add nullable dateOfBirth and gender from the existing UserProfile mapping.
Existing Auth/JWT and profile update behavior are retained; editing these two fields is not added.
Options still come exclusively from active database catalog rows. StylePresentation adds an
optional illustrationIndex and vi/en description fallback; stored descriptions take precedence.
The Flutter image atlas is presentation only and does not define selectable catalog entries.

Proposed seed: database/seeds/002_editorial_catalog_expansion.sql.
Review data and prerequisites: database/seeds/002_editorial_catalog_expansion.md.
It is NOT executed. No schema change or account/profile/preference writes are proposed.
The target is 18 styles, 24 colors and 12 occasions with vi/en translations; existing values
are preserved except missing/blank descriptions are filled. Catalog IDs are resolved by code.
Live database access was unavailable in this session; compatibility was checked against the
project DDL and the user's confirmed keys/counts. Re-running a seed requires separate approval.

Exact files changed for this upgrade: ../FE/EDITORIAL_UPGRADE_FILES.md.
Backend verification: 34 tests pass, 0 failures/errors; fixtures use isolated H2 only.

## Personal profile editing

PUT /api/profile now accepts optional dateOfBirth (YYYY-MM-DD) and gender in addition to
fullName/phoneNumber. Missing optional properties preserve saved values; explicit null clears
them. Gender accepts FEMALE, MALE, OTHER, PREFER_NOT_TO_SAY. Future, invalid and unsupported
dates are rejected; email remains outside the update contract. MySQL DATE's supported lower
bound is 1000-01-01: https://dev.mysql.com/doc/refman/8.0/en/datetime.html.

The existing transaction persists account phone changes and personal details consistently.
No entity/schema, JWT/Auth or avatar storage changes. Restart Spring Boot with the new code;
do not run migrations/seeds. Latest verification: 37/37 backend tests pass on isolated H2.
Coverage includes persistence across login, explicit null clearing, backward compatibility,
all four genders and rejected date/phone/email input. Exact files: ../FE/PERSONAL_INFO_FILES.md.


## Cloudinary image upload

JWT-protected uploads for avatars and admin-only style/outfit images are available at `/api/uploads/{avatars|styles|outfits}`. They return `secure_url` and `public_id` without modifying database records. Existing Profile avatar API remains compatible.

See [Cloudinary setup and Windows upload test](docs/cloudinary-upload.md) for environment variables, permissions, limits and real-image verification.

## Explore catalog, guide and My Wardrobe

Explore APIs: GET /api/explore/styles, /api/explore/styles/{slug}, /api/explore/styles/by-id/{id}.
Exactly 40 enabled Explore styles/5 groups; four original legacy styles remain outside Explore
to preserve IDs/preferences (44 physical style rows total).
Guide JSON is loaded only on detail. Old Money has ten structured sections and six outfit formulas;
the other 39 guides and all catalog/outfit cover images remain missing, with explicit UI fallback.

Database migrations/seeds 003, 004 and 005 have been applied to fashion_ai.
Do not execute their CREATE/ALTER statements again on this database.
No account/profile/preference schema or existing user-data changes.
New wardrobe tables: wardrobe_items, wardrobe_item_styles; existing clothing catalog seeded
with seven roots/21 children and vi/en translations.

JWT wardrobe API and Windows/Android testing: [wardrobe.md](docs/wardrobe.md).
Exact files: [Wardrobe report](../FE/WARDROBE_FILES.md),
[Explore/guide report](../FE/EXPLORE_GUIDE_FILES.md).
Full backend verification: 94 tests pass, Maven package succeeds.
Actual MySQL/Cloudinary create/update/filter/ownership/delete smoke tests pass; test records/assets cleaned.
