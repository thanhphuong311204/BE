package com.datn.fashion.service;

import com.datn.fashion.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import static org.junit.jupiter.api.Assertions.*;

class ImageUploadValidatorTest {
    private final ImageUploadValidator validator = new ImageUploadValidator();
    private byte[] image(String format, int width, int height) throws Exception {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, out);
        return out.toByteArray();
    }
    @ParameterizedTest @ValueSource(strings = {"png", "jpg"})
    void detectsRealFormatDespiteFilenameAndMime(String format) throws Exception {
        var result = validator.validate(new MockMultipartFile("file", "fake.exe", "text/plain", image(format, 2, 2)));
        assertEquals("image." + format, result.filename());
    }
    @Test void decodesWebp() throws Exception {
        try (var input = getClass().getResourceAsStream("/images/valid.webp")) {
            assertNotNull(input);
            var result = validator.validate(new MockMultipartFile("file", "a.webp", "image/webp", input.readAllBytes()));
            assertEquals("image.webp", result.filename());
        }
    }
    @Test void rejectsFakeImage() {
        var e = assertThrows(ApiException.class, () -> validator.validate(new MockMultipartFile("file", "a.jpg", "image/jpeg", "fake".getBytes())));
        assertEquals("INVALID_IMAGE", e.code);
    }
    @Test void rejectsEmptyFile() {
        assertThrows(ApiException.class, () -> validator.validate(new MockMultipartFile("file", new byte[0])));
    }
    @Test void rejectsOversize() {
        var e = assertThrows(ApiException.class, () -> validator.validate(new MockMultipartFile("file", new byte[5*1024*1024+1])));
        assertEquals(413, e.status.value());
    }
    @Test void rejectsGif() throws Exception {
        assertThrows(ApiException.class, () -> validator.validate(new MockMultipartFile("file", image("gif", 2, 2))));
    }
    @Test void rejectsOversizeDimensionsBeforeDecode() throws Exception {
        assertThrows(ApiException.class, () -> validator.validate(new MockMultipartFile("file", image("png", 4097, 1))));
    }
}
