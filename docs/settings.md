# NOVA ATELIER — Settings

## Implemented

- Four groups: account/security, application preferences, personal fashion, information/privacy.
- Existing `PersonalInfoScreen`, `ProfileStyleScreen`/style onboarding editor, and `ProfileWardrobeScreen` are reused.
- Preferences save through the existing `PUT /api/style-preferences` flow and reload after editing.
- Password change: `POST /api/auth/change-password`, authenticated using the existing JWT security filter.
- Privacy/about pages describe the implemented data flow, Cloudinary image hosting, token storage, and current limitations.
- Logout calls the existing `AuthSession.logout()` and existing app navigation removes protected routes.

## Password API

JSON fields: `currentPassword`, `newPassword`, `confirmPassword`. The user ID comes only from the validated JWT subject.

- Current password is checked using the existing BCrypt encoder.
- New password: 6–72 characters, at most 72 UTF-8 bytes, confirmation must match, must differ from the current password.
- Database update uses the existing pessimistic user lock and a transaction.
- Success: `204 No Content`; Flutter's shared client accepts this response without attempting to decode JSON.
- Wrong current password: `400 CURRENT_PASSWORD_INCORRECT`, field `currentPassword`; it does not expire the session.
- Validation: `400 VALIDATION_ERROR` with field errors.
- Same password: `400 PASSWORD_UNCHANGED`.
- Invalid/expired JWT or unavailable account: `401`.
- More than five valid requests per account within a fixed 15-minute window: `429 PASSWORD_CHANGE_RATE_LIMITED`.
- Requests include successful attempts. Invalid DTOs never reach BCrypt or the service.
- DTO diagnostic text redacts all credentials. MVC method/validation diagnostics are configured at WARN to prevent debug logging of rejected password values.

The limiter is thread-safe, bounded to 10,000 account entries, in memory, and local to one backend instance. Restart resets it. Multiple instances require a shared limiter before deployment at that scale. Password changes retain the existing stateless JWT behavior: issued tokens remain valid until expiry. This is disclosed in the UI; no token revocation is claimed.

## Database

Uses the existing `users.password_hash` column in MySQL `fashion_ai`. No migration, catalog changes, or manual production data writes are needed. Automated integration tests run on H2 in MySQL compatibility mode; they do not modify the live MySQL database.

## Deferred

- Language switching: no application localization infrastructure exists. Vietnamese is displayed as a read-only status.
- Notifications: no real notification transport/preferences integration exists. No toggle or fake preference save is provided.
- Firebase FCM, AI analysis, chatbot, account deletion/data export, and token revocation are outside this change.
- The 40-style library remains unchanged.

## Files changed for this task

Backend (`D:\DATN\BE`):

- `src/main/java/com/datn/fashion/dto/auth/ChangePasswordRequest.java` — new request validation and credential redaction.
- `src/main/java/com/datn/fashion/service/PasswordChangeRateLimiter.java` — per-account request limit.
- `src/main/java/com/datn/fashion/service/AuthService.java` — BCrypt verification and password hash update.
- `src/main/java/com/datn/fashion/controller/AuthController.java` — protected password endpoint.
- `src/main/java/com/datn/fashion/exception/GlobalExceptionHandler.java` — maps UTF-8 password validation errors to form fields.
- `src/main/resources/application.yml` — suppresses potentially sensitive MVC debug diagnostics.
- `src/test/java/com/datn/fashion/AuthProfileIntegrationTest.java` — API, BCrypt, JWT, account isolation and rate limit regression tests.
- `src/test/java/com/datn/fashion/service/PasswordChangeRateLimiterTest.java` — limit expiry and request redaction tests.
- `docs/settings.md` — implementation and validation report.

Frontend (`D:\DATN\FE`):

- `lib/features/profile/screens/profile_settings_screen.dart` — Settings groups, working routes, status, logout.
- `lib/features/profile/screens/change_password_screen.dart` — validated password form, loading, field/network errors.
- `lib/features/profile/screens/settings_info_screen.dart` — privacy/about content.
- `lib/features/auth/services/auth_service.dart` — password API call.
- `lib/core/network/api_endpoints.dart` — password endpoint.
- `lib/core/network/api_client.dart` — accepts `204 No Content`.
- `test/settings_test.dart` — Settings routes, profile/preferences saving, logout, password responses, pending requests, and responsive layout preview.
- `SETTINGS_FILES.md` — frontend change report.

## Validation

- Backend: `mvn test` — 103 tests passed, no failures/errors/skips.
- Flutter: see `D:\DATN\FE\SETTINGS_FILES.md` for final analyze/test results.
- Visual preview: `D:\DATN\FE\build\editorial-preview\settings.png` (390 × 844).
