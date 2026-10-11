package com.datn.fashion.service;

import com.datn.fashion.dto.media.ImageUploadResponse;
import com.datn.fashion.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CloudinaryAvatarStorageTest {
    private final CloudinaryImageStorage storage = mock(CloudinaryImageStorage.class);
    private final CloudinaryAvatarStorage avatars = new CloudinaryAvatarStorage(storage);
    @Test void preservesAvatarStorageContractAndFolder() {
        byte[] bytes = {1};
        when(storage.upload(bytes, "avatar.png", ImageUploadTarget.AVATAR, 2L))
            .thenReturn(new ImageUploadResponse("https://res.cloudinary.com/test/a.png", "nova-atelier/users/avatars/2/test"));
        assertEquals("nova-atelier/users/avatars/2/test", avatars.upload(bytes, "avatar.png", 2L).publicId());
        avatars.delete("nova-atelier/users/avatars/2/test");
        verify(storage).delete("nova-atelier/users/avatars/2/test");
    }
    @Test void preservesMissingConfigurationErrorCode() {
        when(storage.upload(any(), anyString(), any(), anyLong()))
            .thenThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CLOUDINARY_NOT_CONFIGURED", "Not configured"));
        assertEquals("AVATAR_NOT_CONFIGURED", assertThrows(ApiException.class,
            () -> avatars.upload(new byte[]{1}, "avatar.png", 2L)).code);
    }
    @Test void preservesUploadFailureErrorCode() {
        when(storage.upload(any(), anyString(), any(), anyLong()))
            .thenThrow(new ApiException(HttpStatus.BAD_GATEWAY, "IMAGE_UPLOAD_FAILED", "Upload failed"));
        assertEquals("AVATAR_UPLOAD_FAILED", assertThrows(ApiException.class,
            () -> avatars.upload(new byte[]{1}, "avatar.png", 2L)).code);
    }
}
