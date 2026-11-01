package com.datn.fashion.dto.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** Public, server-hydrated content. Provider output never supplies image URLs. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record VisualBlock(String type, String text, String title, List<Swatch> colors,
                          List<Outfit> outfits, List<Item> items, List<GalleryImage> images, List<String> tips) {
    public record Swatch(String name,String hex) {}
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record Outfit(String id,String name,String description,List<String> occasions,List<String> items,String imageUrl) {}
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record Item(String id,String name,String category,String imageUrl) {}
    public record GalleryImage(String id,String name,String imageUrl) {}
    public static VisualBlock text(String text) { return new VisualBlock("text",text,null,null,null,null,null,null); }
}
