package com.datn.fashion.service;

import com.datn.fashion.dto.media.ImageUploadResponse;
import com.datn.fashion.entity.User;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaUploadService {
    private final UserRepository users;
    private final JdbcTemplate jdbc;
    private final ImageUploadValidator validator;
    private final CloudinaryImageStorage storage;
    public MediaUploadService(UserRepository users, JdbcTemplate jdbc, ImageUploadValidator validator, CloudinaryImageStorage storage) {
        this.users = users; this.jdbc = jdbc; this.validator = validator; this.storage = storage;
    }
    public ImageUploadResponse upload(Long userId, ImageUploadTarget target, MultipartFile file) {
        var user = users.findById(userId).orElseThrow(() -> unauthorized());
        if (user.status != User.Status.ACTIVE || user.deletedAt != null) throw unauthorized();
        if (target != ImageUploadTarget.AVATAR) {
            Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM user_roles ur JOIN roles r ON r.id = ur.role_id
                WHERE ur.user_id = ? AND r.code = 'ADMIN' AND r.is_active = TRUE
                """, Integer.class, userId);
            if (count == null || count == 0)
                throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Chỉ quản trị viên được tải ảnh thư viện.");
        }
        var image = validator.validate(file);
        // Upload only: no DB writes, no automatic attachment to Profile/style/outfit.
        return storage.upload(image.bytes(), image.filename(), target, userId);
    }
    private ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Tài khoản không còn khả dụng.");
    }
}
