package com.datn.fashion.dto.style;
import jakarta.validation.constraints.*;
import java.util.List;
public record SaveStylePreferencesRequest(
    @NotNull @Size(min = 1, max = 3, message = "Chọn từ 1 đến 3 phong cách.")
    List<@NotNull @Positive Long> styleIds,
    @NotNull @Size(min = 1, max = 6, message = "Chọn từ 1 đến 6 màu.")
    List<@NotNull @Positive Long> colorIds,
    @NotNull @Size(min = 1, max = 5, message = "Chọn từ 1 đến 5 hoàn cảnh.")
    List<@NotNull @Positive Long> occasionIds
) {}
