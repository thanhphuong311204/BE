package com.datn.fashion.dto.wardrobe;
import java.util.List;
public record OutfitRecommendationsResponse(String method, String status, List<String> messages,
    int page, int limit, boolean hasMore, List<Outfit> outfits) {
 public record Outfit(String id, String name, List<WardrobeItemResponse> items, int score, List<String> reasons) {}
}
