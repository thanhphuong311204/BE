# My Wardrobe
See [complete file report and Android test guide](../../FE/WARDROBE_FILES.md).

Database migration 005 and seed 005 were applied to fashion_ai during implementation.
Do not rerun CREATE TABLE against this database. Seed is idempotent, resolves IDs by code,
and reuses existing clothing_categories/translations: seven roots, 21 children, vi/en.
New tables: wardrobe_items and wardrobe_item_styles, with BIGINT UNSIGNED FKs matching MySQL.

All APIs require JWT and an ACTIVE account with deleted_at NULL.
Owner is always derived from JWT, never supplied in request JSON.
A different account receives 404 for detail/update/delete; lists show only its own items.

| Method | Endpoint | Request |
| --- | --- | --- |
| GET | /api/wardrobe/options | active categories(parentId), colors, exact 40 Explore styles |
| GET | /api/wardrobe/items | optional mainCategoryId, subCategoryId, colorId query |
| GET | /api/wardrobe/items/{id} | item detail |
| POST | /api/wardrobe/items | multipart file + item JSON string; returns 201 |
| PUT | /api/wardrobe/items/{id} | JSON metadata; preserves image |
| DELETE | /api/wardrobe/items/{id} | deletes DB row/links; image cleanup after commit, returns 200 |

Metadata: name(required <=150), mainCategoryId(required root), subCategoryId(optional child of root),
colorId(required active), styleIds(optional distinct active Explore IDs, <=40), notes(optional <=1000).
Client cannot supply userId, imageUrl or publicId. Images are decoded as JPG/PNG/WebP;
5 MiB maximum, 4096x4096 maximum dimensions. Upload uses existing CloudinaryImageStorage
under nova-atelier/users/wardrobe/{userId}/{randomUUID}. Regular users do not gain access
to styles/outfits library upload.

Upload starts only when creating the item. Validate account, metadata and actual image contents first.
Persist item/style links in one transaction; rollback cleans the uploaded image.
Delete commits the DB change before cleanup; transient provider cleanup failures log only a safe error
and need maintenance retry. No unrelated Auth/Profile/Avatar handler changes.

The JAR was tested against real MySQL and Cloudinary: options 28 categories/8 colors/40 styles,
POST 201, metadata update 200, matching/unmatched filters, non-owner 404, DELETE 200.
Cloudinary Admin API confirmed the test asset deleted (404). Test DB rows were removed.
Automated BE tests use isolated H2 and mocked Cloudinary transport; 94 tests pass.
Flutter tests mock picker/HTTP and cover both camera/gallery/preview/cancel/CRUD; 77 pass.

Windows:
```powershell
cd D:\DATN\BE
mvn test
mvn package
# Use the existing .env / environment variables. Never print JWT_SECRET/DB_PASSWORD/API_SECRET.
java -jar target\fashion-backend-0.0.1-SNAPSHOT.jar --server.port=8080
```
Stop an existing backend run before starting another on 8080.
The implementation uses no schema auto-update or automatic migration runner.
