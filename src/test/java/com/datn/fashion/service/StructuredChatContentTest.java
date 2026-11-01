package com.datn.fashion.service;

import com.datn.fashion.dto.ai.VisualBlock.*;
import com.datn.fashion.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StructuredChatContentTest {
    final ObjectMapper json=new ObjectMapper();
    final StructuredChatContent parser=new StructuredChatContent(json);
    final String image="https://res.cloudinary.com/test-cloud/image/upload/v1/outfit.png";
    final ChatVisualCatalog.Catalog catalog=new ChatVisualCatalog.Catalog(
        List.of(new Outfit("outfit:1:campus","Outfit đã xác minh","Mô tả đúng ảnh",List.of("Đi học"),List.of("Sơ mi ivory","Jeans"),image)),
        List.of(new Item("item:1:shirt","Sơ mi ivory","Trang phục",image.replace("outfit.png","item.png"))),
        List.of(new GalleryImage("style:1","Tối giản",image.replace("outfit.png","gallery.png"))));
    @Test void supportsAllSixTypesAndOnlyHydratesKnownIds() {
        var result=parser.parse("""
            {"text":"**Gợi ý cho bạn**","blocks":[
            {"type":"outfit_suggestions","refs":["outfit:1:campus"],"imageUrl":"https://evil.invalid/wrong.png"},
            {"type":"color_palette","colors":[{"name":"Ivory","hex":"#f4efe5"},{"name":"bad","hex":"red"}]},
            {"type":"fashion_items","refs":["item:1:shirt","item:1:fake"]},
            {"type":"style_gallery","refs":["style:1"]},
            {"type":"styling_tips","tips":["Giữ phụ kiện gọn."]}]}
            """,catalog);
        assertEquals(6,result.blocks().size());
        assertEquals(image,result.blocks().get(1).outfits().getFirst().imageUrl());
        assertEquals("Outfit đã xác minh",result.blocks().get(1).outfits().getFirst().name());
        assertEquals(1,result.blocks().get(2).colors().size());
        assertEquals("#F4EFE5",result.blocks().get(2).colors().getFirst().hex());
        assertEquals(1,result.blocks().get(3).items().size());
        assertFalse(result.toString().contains("evil.invalid"));
        assertNotNull(result.blocks().get(3).items().getFirst().imageUrl());
        assertEquals("style_gallery",result.blocks().get(4).type());
    }
    @Test void onePhotoCannotRepeatAcrossOutfitItemAndGalleryBlocks() {
        var duplicate=new ChatVisualCatalog.Catalog(catalog.outfits(),
            List.of(new Item("item:1:shirt","Shirt","Item",image)),List.of(new GalleryImage("style:1","Style",image)));
        var result=parser.parse("""
            {"text":"Outfit","blocks":[{"type":"outfit_suggestions","refs":["outfit:1:campus"]},
            {"type":"fashion_items","refs":["item:1:shirt"]},{"type":"style_gallery","refs":["style:1"]}]}
            """,duplicate);
        assertEquals(image,result.blocks().get(1).outfits().getFirst().imageUrl());
        assertNull(result.blocks().get(2).items().getFirst().imageUrl());
        assertTrue(result.blocks().stream().noneMatch(b->b.type().equals("style_gallery")));
    }
    @Test void newPastelIdeaNeverInheritsAnUnrelatedImage() {
        var result=parser.parse("""
            {"text":"Mình đổi sang pastel nhé.","blocks":[{"type":"outfit_suggestions","ideas":[
            {"name":"Pastel đi học","description":"Biến thể mới","items":["Sơ mi hồng nhạt","Jeans trắng"],"imageUrl":"https://evil.invalid/a.png"}]}]}
            """,catalog);
        assertEquals(1,result.blocks().size()); // Invented outfits never become catalog cards.
    }
    @Test void malformedStructuredContentNeverLeaksRawJsonAndLegacyTextWorks() {
        for(String raw:List.of("{broken","[]","```json {} ```","{\"blocks\":[]}","{\"text\":\"\"}"," ","x".repeat(40001))) {
            var ex=assertThrows(ApiException.class,()->parser.parse(raw,catalog));
            assertEquals(502,ex.status.value());
            assertEquals("AI_INVALID_RESPONSE",ex.code);
        }
        assertThrows(ApiException.class,()->parser.parse(null,catalog));
        assertEquals("Chào bạn!",parser.parse("Chào bạn!",catalog).text());
        assertEquals(1,parser.parse("{\"text\":\"Ê, mình đây!\",\"blocks\":[]}",catalog).blocks().size());
        assertEquals("Mình là NOVA.",parser.parse("{\"text\":\"Mình là NOVA.\"}",catalog).text());
    }
    @Test void unknownReferencesAndTypesHaveSafeTextFallback() {
        var r=parser.parse("""
            {"text":"Chưa có ảnh phù hợp.","blocks":[{"type":"outfit_suggestions","refs":["not-in-db"]},
            {"type":"html","text":"<img src='x'>"},{"type":"fashion_items","refs":["forged"]}]}
            """,ChatVisualCatalog.Catalog.empty());
        assertEquals(1,r.blocks().size());
        assertEquals("text",r.blocks().getFirst().type());
    }
    @Test void capsRepeatedBlocksAndProvidesHistorySummaryWithoutImageUrls() {
        String block="{\"type\":\"outfit_suggestions\",\"refs\":[\"outfit:1:campus\",\"outfit:1:campus\"]}";
        var r=parser.parse("{\"text\":\"Gợi ý\",\"blocks\":["+String.join(",",Collections.nCopies(20,block))+"]}",catalog);
        assertEquals(2,r.blocks().size());assertEquals(1,r.blocks().get(1).outfits().size());
        String summary=StructuredChatContent.summary(r.blocks());
        assertTrue(summary.contains("outfit:1:campus"));assertFalse(summary.contains("https://"));
    }
}
