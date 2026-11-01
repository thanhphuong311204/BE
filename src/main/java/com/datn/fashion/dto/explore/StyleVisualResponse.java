package com.datn.fashion.dto.explore;
import java.util.List;

/** Reviewed women-only editorial assets, independent from the preserved guide. */
public record StyleVisualResponse(List<Asset> dna, List<Asset> fabrics,
 List<Asset> essentials, List<String> tips) {
 public record Asset(String key, String name, String imageUrl) {}
 public static StyleVisualResponse empty() {
  return new StyleVisualResponse(List.of(), List.of(), List.of(), List.of());
 }
}
