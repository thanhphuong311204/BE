package com.datn.fashion.service;

import com.datn.fashion.exception.ApiException;
import org.springframework.stereotype.Service;

/** Compatibility adapter: keep the existing Profile API and rollback cleanup contract. */
@Service
public class CloudinaryAvatarStorage implements AvatarStorage {
    private final CloudinaryImageStorage storage;
    public CloudinaryAvatarStorage(CloudinaryImageStorage storage) { this.storage = storage; }
    @Override public UploadedAvatar upload(byte[] bytes, String filename, Long userId) {
        try {
            var result = storage.upload(bytes, filename, ImageUploadTarget.AVATAR, userId);
            return new UploadedAvatar(result.secureUrl(), result.publicId());
        } catch (ApiException e) {
            String code = e.code.equals("CLOUDINARY_NOT_CONFIGURED") ? "AVATAR_NOT_CONFIGURED" : "AVATAR_UPLOAD_FAILED";
            throw new ApiException(e.status, code, e.getMessage());
        }
    }
    @Override public void delete(String publicId) { storage.delete(publicId); }
}
