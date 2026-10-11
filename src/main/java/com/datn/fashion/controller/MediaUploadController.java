package com.datn.fashion.controller;

import com.datn.fashion.dto.media.ImageUploadResponse;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.service.ImageUploadTarget;
import com.datn.fashion.service.MediaUploadService;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class MediaUploadController {
    private final MediaUploadService service;
    public MediaUploadController(MediaUploadService service) { this.service = service; }
    @PostMapping(value = "/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImageUploadResponse upload(@AuthenticationPrincipal Jwt jwt, @PathVariable String type,
                                     @RequestPart("file") MultipartFile file) {
        Long userId;
        try {
            userId = Long.parseLong(jwt.getSubject());
            if (userId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Token không hợp lệ.");
        }
        ImageUploadTarget target = switch (type) {
            case "styles" -> ImageUploadTarget.STYLE;
            case "outfits" -> ImageUploadTarget.OUTFIT;
            case "avatars" -> ImageUploadTarget.AVATAR;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_UPLOAD_TYPE", "Loại ảnh không được hỗ trợ.");
        };
        return service.upload(userId, target, file);
    }
}
