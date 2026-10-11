package com.datn.fashion.dto.wardrobe;
import jakarta.validation.constraints.*;
import java.util.List;
public record WardrobeItemRequest(
 @NotBlank @Size(max=150) String name,
 @NotNull @Positive Long mainCategoryId,
 @Positive Long subCategoryId,
 @NotNull @Positive Long colorId,
 @Size(max=40) List<@NotNull @Positive Long> styleIds,
 @Size(max=1000) String notes) {}
