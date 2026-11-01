package com.datn.fashion.service;

import com.datn.fashion.dto.ai.VisualBlock;
import com.datn.fashion.dto.ai.VisualBlock.*;
import com.datn.fashion.exception.ApiException;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.*;
import org.springframework.stereotype.Service;
import java.util.*;

/** Validates provider JSON, then joins only server-known catalog IDs. No URL is taken from model output. */
@Service
public class StructuredChatContent {
    private final ObjectMapper json;
    public StructuredChatContent(ObjectMapper json){this.json=json;}
    public record Reply(String text,List<VisualBlock> blocks) {}
    public Reply parse(String raw,ChatVisualCatalog.Catalog catalog) {
        if(raw==null || raw.isBlank() || raw.length()>40000) throw invalidResponse();
        String trimmed=raw.strip();
        if(!trimmed.startsWith("{") && !trimmed.startsWith("[") && !trimmed.startsWith("```")) return text(clean(raw,2000)); // Old text providers remain compatible.
        try {
            var root=json.readTree(trimmed);
            if(root==null || !root.isObject() || !root.path("text").isTextual() || root.path("text").asText().isBlank()) throw invalidResponse();
            String reply=clean(root.path("text").asText(),2000);
            if(reply.isBlank() || reply.startsWith("{") || reply.startsWith("```json"))throw invalidResponse();
            var blocks=new ArrayList<VisualBlock>();blocks.add(VisualBlock.text(reply));
            var seen=new HashSet<String>();seen.add("text");
            int imageBudget=8;
            var imageUrls=new HashSet<String>();
            if(root.path("blocks").isArray()) for(var b:root.path("blocks")) {
                if(blocks.size()>=6)break;
                String type=b.path("type").asText();
                if(!seen.add(type))continue;
                String title=clean(b.path("title").asText(),100);
                switch(type) {
                    case "color_palette" -> {
                        var colors=new ArrayList<Swatch>();
                        if(b.path("colors").isArray())for(var c:b.path("colors")) {
                            if(colors.size()==6)break;
                            String hex=c.path("hex").asText();String name=clean(c.path("name").asText(),50);
                            if(hex.matches("#[0-9a-fA-F]{6}")&&!name.isBlank())colors.add(new Swatch(name,hex.toUpperCase(Locale.ROOT)));
                        }
                        if(!colors.isEmpty())blocks.add(new VisualBlock(type,null,title,List.copyOf(colors),null,null,null,null));
                    }
                    case "outfit_suggestions" -> {
                        var outfits=new ArrayList<Outfit>();var ids=new HashSet<String>();
                        for(String id:ChatVisualCatalog.strings(b.path("refs"),3))for(var o:catalog.outfits())if(o.id().equals(id)&&o.imageUrl()!=null&&ids.add(id)) {
                            String image=imageBudget>0 && o.imageUrl()!=null && imageUrls.add(o.imageUrl())?o.imageUrl():null;if(image!=null)imageBudget--;
                            outfits.add(new Outfit(o.id(),o.name(),o.description(),o.occasions(),o.items(),image));
                        }
                        if(!outfits.isEmpty())blocks.add(new VisualBlock(type,null,title,null,List.copyOf(outfits),null,null,null));
                    }
                    case "fashion_items" -> {
                        var items=new ArrayList<Item>();var ids=new HashSet<String>();
                        for(String id:ChatVisualCatalog.strings(b.path("refs"),4))for(var i:catalog.items())if(i.id().equals(id)&&ids.add(id)) {
                            String image=imageBudget>0 && i.imageUrl()!=null && imageUrls.add(i.imageUrl())?i.imageUrl():null;if(image!=null)imageBudget--;
                            items.add(new Item(i.id(),i.name(),i.category(),image));
                        }
                        if(!items.isEmpty())blocks.add(new VisualBlock(type,null,title,null,null,List.copyOf(items),null,null));
                    }
                    case "style_gallery" -> {
                        var images=new ArrayList<GalleryImage>();var ids=new HashSet<String>();
                        for(String id:ChatVisualCatalog.strings(b.path("refs"),8))for(var i:catalog.images())if(i.id().equals(id)&&ids.add(id)&&imageBudget>0&&imageUrls.add(i.imageUrl())) {images.add(i);imageBudget--;}
                        if(!images.isEmpty())blocks.add(new VisualBlock(type,null,title,null,null,null,List.copyOf(images),null));
                    }
                    case "styling_tips" -> {
                        var tips=ChatVisualCatalog.strings(b.path("tips"),5);
                        if(!tips.isEmpty())blocks.add(new VisualBlock(type,null,title,null,null,null,null,tips));
                    }
                    default -> { /* Unknown/malformed blocks never reach the UI. */ }
                }
            }
            return new Reply(reply,List.copyOf(blocks));
        } catch(java.io.IOException ex){throw invalidResponse();}
    }
    /** Server-owned gallery and availability labels, never provider-supplied image URLs. */
    public Reply withPhotoDiscovery(Reply reply,VisualBlock gallery) {
        var blocks=new ArrayList<VisualBlock>();
        blocks.add(VisualBlock.text(reply.text()));
        if(gallery!=null)blocks.add(gallery);
        else blocks.add(new VisualBlock("styling_tips",null,"Từ thư viện NOVA",null,null,null,null,
            List.of("Chưa có thêm ảnh phù hợp đã xác minh trong thư viện NOVA. Mình không thay bằng ảnh không liên quan.")));
        for(var b:reply.blocks()) {
            if(blocks.size()>=6)break;
            if(b.type().equals("text")||b.type().equals("style_gallery"))continue;
            // Gallery receives image priority; keep the outfit/item descriptions without repeated photos.
            if(b.outfits()!=null)blocks.add(new VisualBlock(b.type(),b.text(),b.title(),b.colors(),
                b.outfits().stream().map(o->new Outfit(o.id(),o.name(),o.description(),o.occasions(),o.items(),null)).toList(),b.items(),b.images(),b.tips()));
            else if(b.items()!=null)blocks.add(new VisualBlock(b.type(),b.text(),b.title(),b.colors(),b.outfits(),
                b.items().stream().map(i->new Item(i.id(),i.name(),i.category(),null)).toList(),b.images(),b.tips()));
            else blocks.add(b);
        }
        return new Reply(reply.text(),List.copyOf(blocks));
    }
    private ApiException invalidResponse(){
        org.slf4j.LoggerFactory.getLogger(getClass()).warn("nova stage=structured_response code=AI_INVALID_RESPONSE");
        return new ApiException(HttpStatus.BAD_GATEWAY,"AI_INVALID_RESPONSE",
            "Gemini trả về dữ liệu không hợp lệ. Bạn vui lòng thử lại nhé.");
    }
    private Reply text(String value){return new Reply(value,List.of(VisualBlock.text(value)));}
    private String clean(String s,int max){return ChatVisualCatalog.shortText(s.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]",""),max).strip();}
    public static List<VisualBlock> readBlocks(ObjectMapper json,String value) {
        if(value==null||value.isBlank())return List.of();
        try {List<VisualBlock> blocks=json.readerForListOf(VisualBlock.class).readValue(value);return List.copyOf(blocks);}
        catch(Exception ex){return List.of();}
    }
    public static String summary(List<VisualBlock> blocks) {
        var text=new StringBuilder();
        for(var b:blocks) {
            if(b.outfits()!=null)for(var o:b.outfits())text.append("\nOutfit ").append(o.id()==null?"ý tưởng":o.id()).append(": ").append(o.name()).append(" — ").append(String.join(", ",o.items()));
            if(b.tips()!=null)for(var tip:b.tips())text.append("\nMẹo đã tư vấn: ").append(tip);
            if(b.colors()!=null)for(var c:b.colors())text.append("\nMàu ").append(c.name()).append(' ').append(c.hex());
            if(b.images()!=null)for(var i:b.images())text.append("\nẢnh ").append(i.id()).append(": ").append(i.name());
            if(b.items()!=null)for(var i:b.items())text.append("\nMón đồ ").append(i.id()).append(": ").append(i.name());
        }
        return ChatVisualCatalog.shortText(text.toString(),1800);
    }
}
