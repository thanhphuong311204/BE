package com.datn.fashion.dto.wardrobe;
import java.util.List;
import java.time.LocalDateTime;
public record WardrobeItemResponse(Long id,String name,Long mainCategoryId,Long subCategoryId,Long colorId,
 String imageUrl,String notes,List<Long> styleIds,LocalDateTime createdAt,LocalDateTime updatedAt) {}
