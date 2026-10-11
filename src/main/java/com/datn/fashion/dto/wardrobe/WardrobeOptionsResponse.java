package com.datn.fashion.dto.wardrobe;
import java.util.List;
import com.datn.fashion.dto.explore.ExploreStyleResponse;
public record WardrobeOptionsResponse(List<Category> categories,List<Color> colors,List<ExploreStyleResponse> styles) {
 public record Category(Long id,Long parentId,String code,String name,String nameEn) {}
 public record Color(Long id,String code,String name,String hexCode) {}
}
