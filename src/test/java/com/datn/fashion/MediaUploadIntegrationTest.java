package com.datn.fashion;

import com.datn.fashion.dto.media.ImageUploadResponse;
import com.datn.fashion.entity.User;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.security.JwtService;
import com.datn.fashion.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.time.Instant;
import javax.imageio.ImageIO;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:media_upload_test;MODE=MySQL;DB_CLOSE_DELAY=-1") @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class MediaUploadIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired JwtService jwt;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean CloudinaryImageStorage storage;
    private User user;
    private String token;
    @BeforeEach void setup() {
        // H2-only role fixtures. Never execute against the development MySQL database.
        jdbc.execute("CREATE TABLE IF NOT EXISTS roles (id BIGINT PRIMARY KEY, code VARCHAR(50) UNIQUE NOT NULL, is_active BOOLEAN NOT NULL)");
        jdbc.execute("CREATE TABLE IF NOT EXISTS user_roles (user_id BIGINT NOT NULL, role_id BIGINT NOT NULL, PRIMARY KEY(user_id,role_id), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY(role_id) REFERENCES roles(id))");
        user = new User(); user.email = "upload-test@example.com"; user.passwordHash = "unused-test-password-hash";
        user = users.saveAndFlush(user); token = jwt.create(user.id);
        when(storage.upload(any(), anyString(), any(), anyLong())).thenAnswer(invocation ->
            new ImageUploadResponse("https://res.cloudinary.com/test/image/upload/a.png",
                ((ImageUploadTarget) invocation.getArgument(2)).folder() + "/" + invocation.getArgument(3) + "/test"));
    }
    private MockMultipartFile file() throws Exception {
        var out = new ByteArrayOutputStream(); ImageIO.write(new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB), "png", out);
        return new MockMultipartFile("file", "avatar.png", "image/png", out.toByteArray());
    }
    @Test void avatarUploadReturnsProviderIdsWithoutUpdatingProfile() throws Exception {
        mvc.perform(multipart("/api/uploads/avatars").file(file()).param("public_id", "nova-atelier/styles/other")
            .param("userId", "999").header("Authorization", "Bearer " + token))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.secure_url").exists())
            .andExpect(jsonPath("$.public_id").value("nova-atelier/users/avatars/" + user.id + "/test"));
        verify(storage).upload(any(), eq("image.png"), eq(ImageUploadTarget.AVATAR), eq(user.id));
        org.junit.jupiter.api.Assertions.assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM user_profiles", Integer.class));
    }
    @Test void missingJwtIsRejected() throws Exception {
        mvc.perform(multipart("/api/uploads/avatars").file(file())).andExpect(status().isUnauthorized());
        verifyNoInteractions(storage);
    }
    @Test void invalidJwtIsRejected() throws Exception {
        mvc.perform(multipart("/api/uploads/avatars").file(file()).header("Authorization", "Bearer invalid"))
            .andExpect(status().isUnauthorized()); verifyNoInteractions(storage);
    }
    @Test void ordinaryUserCannotUploadLibraryImages() throws Exception {
        for (String type : new String[]{"styles", "outfits"})
            mvc.perform(multipart("/api/uploads/" + type).file(file()).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        verifyNoInteractions(storage);
    }
    @Test void activeAdminCanUploadStylesAndOutfits() throws Exception {
        jdbc.update("INSERT INTO roles(id,code,is_active) VALUES(9001,'ADMIN',TRUE)");
        jdbc.update("INSERT INTO user_roles(user_id,role_id) VALUES(?,9001)", user.id);
        for (String type : new String[]{"styles", "outfits"})
            mvc.perform(multipart("/api/uploads/" + type).file(file()).header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.public_id").value("nova-atelier/" + type + "/" + user.id + "/test"));
    }
    @Test void inactiveAdminRoleCannotUploadStyles() throws Exception {
        jdbc.update("INSERT INTO roles(id,code,is_active) VALUES(9001,'ADMIN',FALSE)");
        jdbc.update("INSERT INTO user_roles(user_id,role_id) VALUES(?,9001)", user.id);
        mvc.perform(multipart("/api/uploads/styles").file(file()).header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden()); verifyNoInteractions(storage);
    }
    @Test void suspendedAccountCannotUpload() throws Exception {
        user.status = User.Status.SUSPENDED; users.saveAndFlush(user);
        mvc.perform(multipart("/api/uploads/avatars").file(file()).header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized()); verifyNoInteractions(storage);
    }
    @Test void deletedAccountCannotUpload() throws Exception {
        user.deletedAt = Instant.now(); users.saveAndFlush(user);
        mvc.perform(multipart("/api/uploads/avatars").file(file()).header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized()); verifyNoInteractions(storage);
    }
    @Test void invalidImageAndUnknownTypeAreRejected() throws Exception {
        mvc.perform(multipart("/api/uploads/avatars").file(new MockMultipartFile("file", "a.png", "image/png", "fake".getBytes()))
            .header("Authorization", "Bearer " + token)).andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/uploads/other").file(file()).header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest()); verifyNoInteractions(storage);
    }
    @Test void missingFileIsRejected() throws Exception {
        mvc.perform(multipart("/api/uploads/avatars").header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest()); verifyNoInteractions(storage);
    }
    @Test void oversizeFileIsRejected() throws Exception {
        mvc.perform(multipart("/api/uploads/avatars").file(new MockMultipartFile("file", new byte[5*1024*1024+1]))
            .header("Authorization", "Bearer " + token)).andExpect(status().isPayloadTooLarge()); verifyNoInteractions(storage);
    }
    @Test void unconfiguredStorageReturns503() throws Exception {
        doThrow(new com.datn.fashion.exception.ApiException(
            org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "CLOUDINARY_NOT_CONFIGURED", "Not configured"))
            .when(storage).upload(any(), anyString(), any(), anyLong());
        mvc.perform(multipart("/api/uploads/avatars").file(file()).header("Authorization", "Bearer " + token))
            .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.code").value("CLOUDINARY_NOT_CONFIGURED"));
    }
}
