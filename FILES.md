# Account/Profile refactor — file changes

Paths below are relative to D:\DATN\BE. Build output in target/ is generated and excluded.

## CREATED

| File | Responsibility |
| --- | --- |
| database/migrations/001_add_phone_number.sql | Only approved MySQL change: nullable phone_number VARCHAR(20), UNIQUE uq_users_phone_number. Run manually once; not auto-executed. |
| src/main/java/com/datn/fashion/entity/UserProfile.java | Existing user_profiles mapping; @MapsId shared PK/FK one-to-one; name/avatar/DOB/gender/bio/timestamps. |
| src/main/java/com/datn/fashion/repository/UserProfileRepository.java | Read and persist profiles by user_id. |
| src/test/resources/schema-test.sql | Independent H2 account/profile schema and constraints; prevents entity generation from hiding nonexistent columns. |

## MODIFIED

| File | Responsibility / change |
| --- | --- |
| src/main/java/com/datn/fashion/entity/User.java | Account columns only; nullable phone, required hash, status, email verification and login/soft-delete timestamps. |
| src/main/java/com/datn/fashion/service/AuthService.java | Atomic account/profile registration; normalized email/phone lookup, ACTIVE/nondeleted checks, BCrypt, last_login_at and JWT. |
| src/main/java/com/datn/fashion/service/ProfileService.java | Aggregates account/profile; name/avatar in profiles, phone in accounts; URL max1000; cleans only new asset on rollback. |
| src/main/java/com/datn/fashion/dto/profile/ProfileResponse.java | Same five JSON fields from two entities; missing legacy phone represented as empty response string without database mutation. |
| src/main/java/com/datn/fashion/dto/auth/RegisterRequest.java | Full name max150 and phone max20; retains existing password and phone-format validation. |
| src/main/java/com/datn/fashion/dto/profile/UpdateProfileRequest.java | Name max150 and phone max20; same editable fields. |
| src/test/resources/application-test.yml | Explicit H2 schema initialization and Hibernate validate; no production MySQL access. |
| src/test/java/com/datn/fashion/AuthProfileIntegrationTest.java | 21 integration tests including transaction rollback, status/soft-delete, aggregation, correct-table updates and avatar URL rollback cleanup. |
| README.md | Current schema, manual migration, API compatibility, run/test instructions and limitations. |
| FILES.md | Exact file changes and responsibilities for this refactor. |

## DELETED

| File | Reason |
| --- | --- |
| database/create_users.sql | Obsolete bootstrap definition mixed account/profile fields and contradicted the existing schema. Removing this file does not change any database table. |

## MOVED

None.

## Verification

mvn -B test: 21 tests, 0 failures, 0 errors, 0 skipped.
Hibernate validates mappings against independent H2 DDL.
Cloudinary mocked; live MySQL/Cloudinary not accessed.
No Flutter, controllers, SecurityConfig, runtime application.yml or dependencies changed.
Only phone migration SQL was created; no live ALTER/DROP/CREATE or data update executed.
