# Cloudinary uploads — NOVA ATELIER

## Scope and API

Cloudinary credentials are read from environment variables. Spring also loads the optional ignored `.env` file; environment variables take precedence. No credentials belong in source control or Flutter.

The existing signed REST transport is reused; no Cloudinary SDK dependency is needed. `imageio-webp:3.12.0` adds actual WebP decoding to JDK ImageIO.

| Endpoint | Permission | Cloudinary public ID prefix |
| --- | --- | --- |
| `POST /api/uploads/avatars` | Authenticated ACTIVE, non-deleted account | `nova-atelier/users/avatars/<userId>/<uuid>` |
| `POST /api/uploads/styles` | Same account checks + active `ADMIN` role in `roles`/`user_roles` | `nova-atelier/styles/<userId>/<uuid>` |
| `POST /api/uploads/outfits` | Same account checks + active `ADMIN` role in `roles`/`user_roles` | `nova-atelier/outfits/<userId>/<uuid>` |

Requests are `multipart/form-data` with part `file` and `Authorization: Bearer <JWT>`. Success is HTTP 201:

```json
{
  "secure_url": "https://res.cloudinary.com/<cloud>/image/upload/...",
  "public_id": "nova-atelier/users/avatars/<userId>/<uuid>"
}
```

The server chooses paths and random IDs, signs uploads, and sets `overwrite=false`. Client-supplied `public_id`, folder, or userId never control storage paths. Role checks use the existing database tables; no JWT claim or Auth change is required. If no active ADMIN role is assigned, library uploads remain forbidden. This implementation does not create or assign roles.

The new API only uploads; it does not update Profile, styles, outfits or any database row. To upload AND persist the current user's avatar, use the existing `POST /api/profile/avatar` API, whose ProfileResponse contract and JPEG/PNG policy are unchanged. Its Cloudinary path now uses `nova-atelier/users/avatars`. Rollback cleanup remains supported. Old assets are not automatically deleted or moved.

## Validation and errors

- New API accepts actual decodable JPEG/JPG, PNG, WebP; filename and MIME type do not establish validity.
- Maximum file size: 5 MiB (5 × 1024 × 1024 bytes). Multipart request limit: 6 MiB.
- Maximum image dimensions: 4096 × 4096 pixels.
- Provider connect timeout: 10 seconds; read timeout: 30 seconds.
- 400: invalid/empty/unsupported image, missing `file`, invalid upload type.
- 401: missing/invalid JWT or unavailable account.
- 403: library upload without an active ADMIN role.
- 413: file or multipart request too large.
- 503 / `CLOUDINARY_NOT_CONFIGURED`: configuration missing or cloud-name invalid.
- 502 / `IMAGE_UPLOAD_FAILED`: provider rejection, timeout or invalid response. Check credentials/network in Cloudinary Console; provider response bodies are not exposed.

Cloudinary multipart transport logging is kept at WARN to avoid printing API keys/signatures. Application code does not log passwords, JWTs or Cloudinary secrets. Do not enable HTTP body/Authorization tracing when testing.

## Configure on Windows PowerShell

Open PowerShell in the backend folder. Stop any old backend process occupying port 8080, then configure the same terminal that will launch Spring Boot:

```powershell
Set-Location D:\DATN\BE
$env:CLOUDINARY_CLOUD_NAME = Read-Host 'Cloudinary Cloud Name'
$env:CLOUDINARY_API_KEY = Read-Host 'Cloudinary API Key'
$env:CLOUDINARY_API_SECRET = [System.Net.NetworkCredential]::new('', (Read-Host 'Cloudinary API Secret' -AsSecureString)).Password
$env:CLOUDINARY_DYNAMIC_FOLDERS = 'true'
mvn spring-boot:run
```

Cloud Name is your product environment identifier, not a MySQL username or your Cloudinary account email. Keep DB_URL, DB_USERNAME, DB_PASSWORD and JWT_SECRET configured as before; do not overwrite working database configuration.

These variables apply to this PowerShell session and child processes. To configure later sessions, add the three values to the ignored `D:\DATN\BE\.env` locally using the keys in `.env.example`. Do not commit `.env`. Restart Spring Boot after changing configuration.

For modern Cloudinary dynamic folder mode, `asset_folder` organizes files in the Media Library under `nova-atelier/styles`, `nova-atelier/outfits` or `nova-atelier/users/avatars`. The public ID independently contains the user ID and UUID. If your product environment uses legacy fixed folder mode, set `CLOUDINARY_DYNAMIC_FOLDERS=false`; public ID paths provide the folder hierarchy there.

## Test one real image

Keep Spring Boot running and open another PowerShell terminal. Use an existing account; registration/database changes are not needed for this test.

```powershell
$identifier = Read-Host 'Email or phone number'
$password = [System.Net.NetworkCredential]::new('', (Read-Host 'Password' -AsSecureString)).Password
$body = @{ identifier = $identifier; password = $password } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body $body
Remove-Variable password, body
$imagePath = Read-Host 'Absolute path to a JPG, PNG or WebP image (without quotes)'
if (-not (Test-Path -LiteralPath $imagePath -PathType Leaf)) { throw 'Image file does not exist' }
curl.exe --silent --show-error --fail-with-body -X POST 'http://localhost:8080/api/uploads/avatars' -H "Authorization: Bearer $($login.token)" -F "file=@$imagePath"
Remove-Variable login
```

Do not print `$login` or the JWT. Expected output has `secure_url` and `public_id`. Open secure_url in a browser and confirm the asset in Cloudinary Media Library. The account/Profile remains unchanged by this upload-only API. Test file removal can be done manually in Cloudinary Console.

Use Postman instead if curl is unavailable: POST the avatar endpoint, select Bearer Token using your login JWT, Body → form-data → `file` of type File, and choose an image. Let Postman generate multipart Content-Type and boundary.

For styles/outfits, repeat with the corresponding endpoint and an account already assigned the active ADMIN role. Ordinary users should receive 403. No database permission assignment or SQL is executed by this feature.

## Automated verification

```powershell
Set-Location D:\DATN\BE
mvn -B -Ddebug=false verify
```

Tests use H2 and mocked Cloudinary HTTP/storage, including real WebP decoding, signed multipart payloads, ownership, role/status checks, errors and avatar compatibility. H2 role fixtures exist only inside the upload integration test database. Automated tests do not upload to a real Cloudinary account or execute SQL against MySQL.

References: [Cloudinary Upload API](https://cloudinary.com/documentation/image_upload_api_reference), [folder behavior](https://cloudinary.com/documentation/upload_parameters), [TwelveMonkeys ImageIO](https://github.com/haraldk/TwelveMonkeys).


## Diagnose IMAGE_UPLOAD_FAILED safely

HTTP 502 is the application response; the provider may have returned a different HTTP status. Upload failures now log only the exception type, provider HTTP status, an allowlisted reason and folder mode. No provider body, exception message, API key, secret, signature or throwable stack trace is logged.

Example:

```text
Cloudinary upload failed: exceptionType=Unauthorized, providerStatus=401, reason=INVALID_CLOUD_NAME, dynamicFolders=true
```

`INVALID_CLOUD_NAME` means the Cloudinary upload response rejected the supplied Cloud Name. Copy the **Cloud Name of the product environment containing the API Key** from Cloudinary Console. Do not substitute the environment display name, account email or database username. Configuration presence/syntax checks do not prove the credentials belong to a valid environment.

For configuration in `.env`, correct `CLOUDINARY_CLOUD_NAME` there. For session environment configuration, set `$env:CLOUDINARY_CLOUD_NAME` in the PowerShell terminal that starts Spring Boot. Environment variables override `.env`; correcting the file alone will not change a conflicting session variable. Restart Spring Boot after changing either source. Never print API_SECRET, JWT or passwords when diagnosing configuration.

Other safe reasons include `INVALID_API_KEY`, `INVALID_SIGNATURE`, `FOLDER_MODE_REJECTED`, `TIMESTAMP_REJECTED`, `NETWORK_TIMEOUT`, `DNS_ERROR`, `TLS_ERROR`, `CONNECTION_ERROR`, `PROVIDER_UNAVAILABLE` and `UNEXPECTED_PROVIDER_RESPONSE`. Unrecognized provider messages stay classified as `PROVIDER_REJECTED`; raw messages are deliberately not logged.

The configuration log reports presence and Cloud Name syntax only. API Key/Secret authentication cannot be verified until Cloudinary accepts the Cloud Name; do not infer that nonempty credentials are valid.
