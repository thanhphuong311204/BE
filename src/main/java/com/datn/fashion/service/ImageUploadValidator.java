package com.datn.fashion.service;

import com.datn.fashion.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Component
public class ImageUploadValidator {
    public static final long MAX_BYTES = 5L * 1024 * 1024;
    public record ValidatedImage(byte[] bytes, String filename) {}

    public ValidatedImage validate(MultipartFile file) {
        if (file.isEmpty()) throw invalid();
        if (file.getSize() > MAX_BYTES)
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "Ảnh tối đa 5 MB.");
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length > MAX_BYTES)
                throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "Ảnh tối đa 5 MB.");
            // Determine and decode the actual format; ignore client filename and MIME type.
            try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                if (input == null) throw invalid();
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw invalid();
                var reader = readers.next();
                try {
                    String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                    if (!Set.of("jpeg", "jpg", "png", "webp").contains(format)) throw invalid();
                    reader.setInput(input, true, true);
                    int width = reader.getWidth(0), height = reader.getHeight(0);
                    if (width < 1 || height < 1 || width > 4096 || height > 4096) throw invalid();
                    if (reader.read(0) == null) throw invalid();
                    return new ValidatedImage(bytes, "image." + (format.equals("jpeg") ? "jpg" : format));
                } finally { reader.dispose(); }
            }
        } catch (IOException | IllegalArgumentException e) { throw invalid(); }
    }
    private ApiException invalid() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_IMAGE",
            "Chỉ nhận ảnh JPG, PNG, WebP hợp lệ, tối đa 4096 × 4096 pixel.");
    }
}
