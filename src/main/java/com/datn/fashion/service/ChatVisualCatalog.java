package com.datn.fashion.service;

import com.datn.fashion.dto.ai.*;
import com.datn.fashion.dto.ai.VisualBlock.*;
import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.text.Normalizer;
import java.util.*;

/** Bounded read-only retrieval. These are illustrations from the editorial catalog, never the user's wardrobe. */
@Service
public class ChatVisualCatalog {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final String cloud;
    public ChatVisualCatalog(JdbcTemplate jdbc,ObjectMapper json,@Value("${app.cloudinary.cloud-name:}") String cloud) {
        this.jdbc=jdbc;this.json=json;this.cloud=cloud;
    }
    public record Catalog(List<Outfit> outfits,List<Item> items,List<GalleryImage> images,Map<String,String> outfitStyles) {
        public Catalog(List<Outfit> outfits,List<Item> items,List<GalleryImage> images) {this(outfits,items,images,Map.of());}
        public static Catalog empty(){return new Catalog(List.of(),List.of(),List.of());}
        public Map<String,Object> promptData() {
            return Map.of("outfits",outfits.stream().map(o->Map.of("hasImage",o.imageUrl()!=null,"id",o.id(),"style",outfitStyles.getOrDefault(o.id(),""),"name",o.name(),"description",o.description(),"occasions",o.occasions(),"items",o.items())).toList(),
                "items",items.stream().map(i->Map.of("id",i.id(),"name",i.name(),"category",i.category())).toList(),
                "gallery",images.stream().map(i->Map.of("id",i.id(),"name",i.name())).toList());
        }
    }
    public static String normalized(String s) {return Normalizer.normalize(s.toLowerCase(Locale.ROOT),Normalizer.Form.NFD).replaceAll("\\p{M}","").replace('đ','d');}
    public boolean trustedUrl(String url) {
        if(url==null || cloud.isBlank() || url.length()>1000) return false;
        try {
            var u=URI.create(url);
            return "https".equals(u.getScheme()) && "res.cloudinary.com".equals(u.getHost()) && u.getUserInfo()==null &&
                u.getPort()==-1 && u.getQuery()==null && u.getFragment()==null && u.getPath().startsWith("/"+cloud+"/image/upload/") &&
                !u.getPath().contains("..") && u.getPath().matches(".*\\.(png|jpe?g|webp)$");
        } catch(IllegalArgumentException ex){return false;}
    }
    public Catalog retrieve(String message,List<ChatRequest.Turn> history) {
        return retrieve(message,history,Set.of());
    }
    public Catalog retrieve(String message,List<ChatRequest.Turn> history,Set<String> excludedImages) {
        String current=normalized(message).strip();
        if(noPhotos(message)) return Catalog.empty();
        if(current.matches("(e+ nay|e+|hi|hello|xin chao|chao( ban)?|cam on( ban)?|ok)[!. ?]*")) return Catalog.empty();
        String combined=message;
        if(history!=null && current.matches(".*(them anh|them hinh|outfit khac|nhu tren).*")) for(var turn:history) if(turn.role()==ChatRequest.Role.user) combined+=" "+turn.text();
        String normalized=normalized(combined);
        var terms=new LinkedHashSet<String>();
        if(normalized.contains("di hoc") || normalized.contains("hoc duong")){terms.add("học");terms.add("preppy");terms.add("campus");}
        if(normalized.contains("di lam")){terms.add("công sở");terms.add("office");}
        var stop=Set.of("goi","cho","toi","phoi","phong","cach","outfit","style","trang","phuc","voi","sang","doi","mot","nhung","nao","nay","ban","hay","them","giup","minh","mau","duoc","the","cac","rat","ve","va","cua","trong");
        for(String token:normalized.split("[^a-z0-9-]+")) if(token.length()>=3 && !stop.contains(token) && terms.size()<6) terms.add(token);
        if(terms.isEmpty()) {
            if(!normalized.matches(".*(outfit|phong cach|trang phuc|phoi do|quan ao|chat lieu|phu kien).*")) return Catalog.empty();
            return Catalog.empty();
        }
        var conditions=new ArrayList<String>();var args=new ArrayList<Object>();
        for(String term:terms) {conditions.add("(LOWER(CONCAT(COALESCE(s.slug,''),' ',COALESCE(t.name,''),' ',COALESCE(g.content,''))) LIKE ?)");args.add("%"+term.replace("%","").replace("_","")+"%");}
        String focus=message;
        if(history!=null && topic(focus).isEmpty() && current.matches(".*(them anh|nhieu anh|them hinh|nhieu hinh|anh khac|outfit khac).*"))
            for(int i=history.size()-1;i>=0;i--)if(history.get(i).role()==ChatRequest.Role.user){focus=history.get(i).text();break;}
        var preferred=topic(focus);
        String ordering="s.sort_order,s.id";
        if(!preferred.isEmpty()) {
            ordering="CASE WHEN s.slug IN ("+String.join(",",Collections.nCopies(preferred.size(),"?"))+") THEN 0 ELSE 1 END,"+ordering;
            args.addAll(preferred);
        }
        var rows=jdbc.queryForList("SELECT s.id,s.slug,s.image_url,t.name,g.content FROM styles s JOIN languages l ON l.code='vi' " +
            "LEFT JOIN style_translations t ON t.style_id=s.id AND t.language_id=l.id LEFT JOIN style_guides g ON g.style_id=s.id AND g.language_id=l.id " +
            "WHERE s.is_active=TRUE AND s.is_explore_enabled=TRUE AND ("+String.join(" OR ",conditions)+") ORDER BY "+ordering+" LIMIT 3",args.toArray());
        boolean photos=requestedPhotos(message)>0;
        int outfitLimit=photos?8:6, perStyleLimit=photos?8:3;
        var outfits=new ArrayList<Outfit>();var items=new ArrayList<Item>();var gallery=new ArrayList<GalleryImage>();
        var usedImages=new HashSet<>(excludedImages);
        var outfitStyles=new HashMap<String,String>();
        for(var row:rows) {
            long style=((Number)row.get("id")).longValue();String styleName=Objects.toString(row.get("name"),row.get("slug").toString());
            try {
                var guide=json.readTree(Objects.toString(row.get("content"),"{}"));
                var candidates=new ArrayList<JsonNode>();guide.path("outfits").forEach(candidates::add);
                candidates.sort(Comparator.comparingInt((JsonNode o)->score(o,terms)).reversed());
                int added=0;
                for(var o:candidates) {
                    if(added>=perStyleLimit || outfits.size()>=outfitLimit) break;
                    if(!relevant(focus, o.toString(), row.get("slug")+" "+styleName))continue;
                    String key=o.path("key").asText();if(!key.matches("[a-zA-Z0-9_-]{1,100}"))continue;
                    var images=jdbc.queryForList("SELECT image_url,public_id,sha256,provenance FROM style_outfit_images WHERE style_id=? AND outfit_key=? ORDER BY id LIMIT 2",style,key);
                    String image=null;boolean alreadyShown=false;
                    for(var a:images) {
                        String url=Objects.toString(a.get("image_url"),"");String provenance=Objects.toString(a.get("provenance"),"");String publicId=Objects.toString(a.get("public_id"),"");
                        if(usedImages.contains(url)){alreadyShown=true;continue;}
                        if(trustedUrl(url) && !url.equals(row.get("image_url")) && publicId.length()>10 && url.contains("/"+publicId+".") &&
                            Objects.toString(a.get("sha256"),"").matches("[a-f0-9]{64}") && reviewed(provenance)) {image=url;break;}
                    }
                    if(image==null && alreadyShown)continue;
                    if(image!=null)usedImages.add(image);
                    added++;
                    var outfit=new Outfit("outfit:"+style+":"+key,shortText(o.path("name").asText(key),100),shortText(o.path("explanation").asText(),260),strings(o.path("occasions"),3),strings(o.path("items"),6),image);
                    outfits.add(outfit);
                    outfitStyles.put(outfit.id(),row.get("slug")+" "+styleName);
                    if(image!=null)gallery.add(new GalleryImage(outfit.id(),styleName+" — "+outfit.name(),image));
                }
                var profiles=jdbc.queryForList("SELECT content,provenance FROM style_visual_profiles WHERE style_id=?",style);
                for(var profile:profiles) {
                    var data=json.readTree(profile.get("content").toString());var provenance=json.readTree(profile.get("provenance").toString());
                    for(String group:List.of("dna","essentials","fabrics")) for(var asset:data.path(group)) {
                        if(items.size()>=8)break;
                        if(!relevant(focus,asset.toString(),row.get("slug")+" "+styleName))continue;
                        String key=asset.path("key").asText();String url=asset.path("imageUrl").asText();var source=provenance.path(key);
                        if(!usedImages.contains(url) && trustedUrl(url) && source.path("reviewed").isBoolean() && source.path("reviewed").asBoolean(false) && source.path("imageUrl").asText().equals(url) && source.path("sha256").asText().matches("[a-f0-9]{64}"))
                            { usedImages.add(url); items.add(new Item("item:"+style+":"+key,shortText(asset.path("name").asText(),100),group.equals("fabrics")?"Chất liệu":"Trang phục / phụ kiện",url)); }
                    }
                }
            } catch(java.io.IOException ex) {org.slf4j.LoggerFactory.getLogger(getClass()).warn("Skipping malformed editorial metadata");}
        }
        return new Catalog(List.copyOf(outfits),List.copyOf(items),List.copyOf(gallery),Map.copyOf(outfitStyles));
    }
    // Conservative editorial discovery: only recognized fashion topics and matching verified photos.
    // General questions and uncertain follow-ups never receive a fabricated AI answer.
    static List<String> topic(String message) {
        String s=normalized(message);
        if(s.contains("old money"))return List.of("old-money","old money");
        if(s.matches(".*(hoc duong|di hoc|preppy|campus).*"))return List.of("di hoc","hoc duong","preppy","campus");
        if(s.matches(".*(di lam|cong so|office).*"))return List.of("di lam","cong so","office");
        if(s.contains("minimalist")||s.contains("toi gian"))return List.of("minimalist","toi gian");
        if(s.contains("vintage"))return List.of("vintage");
        if(s.contains("casual"))return List.of("casual");
        return List.of();
    }
    public static int requestedPhotos(String message) {
        String s=normalized(message);
        if(noPhotos(message))return 0;
        if(!s.matches("(?s).*\\b(anh|hinh|photos|images|gallery)\\b.*"))return 0;
        var count=java.util.regex.Pattern.compile("(?<![0-9])([0-9]{1,3})\\s*(anh|hinh|photos|images)\\b").matcher(s);
        if(count.find())return Math.max(1,Integer.parseInt(count.group(1)));
        return 8;
    }
    private List<String> resolvedPhotoTopic(String message,List<ChatRequest.Turn> history) {
        var terms=topic(message);
        if(terms.isEmpty() && requestedPhotos(message)>0 && history!=null)
            for(int i=history.size()-1;i>=0;i--)if(history.get(i).role()==ChatRequest.Role.user){terms=topic(history.get(i).text());break;}
        return terms;
    }
    public boolean hasPhotoTopic(String message,List<ChatRequest.Turn> history) {
        return requestedPhotos(message)>0 && !resolvedPhotoTopic(message,history).isEmpty();
    }
    public VisualBlock photoGallery(String message,List<ChatRequest.Turn> history,Catalog data) {
        if(!hasPhotoTopic(message,history))return null;
        var terms=resolvedPhotoTopic(message,history);
        var seen=new HashSet<String>();
        var images=data.outfits().stream().filter(o->o.imageUrl()!=null&&trustedUrl(o.imageUrl()))
            .filter(o->terms.stream().anyMatch(normalized(o.name()+" "+o.description()+" "+String.join(" ",o.occasions())+" "+data.outfitStyles().getOrDefault(o.id(),""))::contains))
            .filter(o->seen.add(o.imageUrl())).limit(Math.min(8,requestedPhotos(message)))
            .map(o->new GalleryImage(o.id(),o.name(),o.imageUrl())).toList();
        if(images.isEmpty())return null;
        int requested=requestedPhotos(message);
        String title=requested>images.size()
            ?"Thư viện NOVA: hiện chỉ có "+images.size()+" ảnh phù hợp chưa hiển thị (bạn yêu cầu "+requested+")"
            :"Ảnh minh họa từ thư viện NOVA";
        return new VisualBlock("style_gallery",null,title,null,null,null,images,null);
    }
    public List<VisualBlock> librarySuggestions(String message,List<ChatRequest.Turn> history,Catalog data) {
        String fashion=".*\\b(outfit|phong cach|trang phuc|phoi do|quan ao|mac|style|preppy|minimalist|vintage|casual)\\b.*";
        var terms=normalized(message).matches(fashion)?topic(message):List.<String>of();
        if(terms.isEmpty() && normalized(message).matches(".*(them anh|nhieu anh|them hinh|nhieu hinh|anh khac|outfit khac).*")) {
            // Only the latest user turn may resolve an otherwise explicit request for more photos.
            for(int i=history.size()-1;i>=0;i--)if(history.get(i).role()==ChatRequest.Role.user) {
                String previous=history.get(i).text();
                terms=normalized(previous).matches(fashion)?topic(previous):List.of();break;
            }
        }
        if(terms.isEmpty())return List.of();
        if(requestedPhotos(message)>0){var gallery=photoGallery(message,history,data);return gallery==null?List.of():List.of(gallery);}
        final var match=terms;
        var seen=new HashSet<String>();
        var outfits=data.outfits().stream().filter(o->o.imageUrl()!=null && trustedUrl(o.imageUrl()))
            .filter(o->match.stream().anyMatch(normalized(o.name()+" "+o.description()+" "+String.join(" ",o.occasions())+" "+data.outfitStyles().getOrDefault(o.id(),""))::contains))
            .filter(o->seen.add(o.imageUrl())).limit(3).toList();
        return outfits.isEmpty()?List.of():List.of(new VisualBlock("outfit_suggestions",null,"Outfit từ thư viện NOVA",null,outfits,null,null,null));
    }
    static boolean noPhotos(String message) {
        return normalized(message).matches("(?s).*(khong (can|muon|dung|hien thi) (anh|hinh)|chi (can )?(van ban|text)).*");
    }
    // Every explicit dimension must match; style labels alone cannot prove garment/color relevance.
    static boolean relevant(String message,String metadata,String style) {
        if(noPhotos(message))return false;
        String query=normalized(message).replaceAll("[^a-z0-9 ]"," "), content=normalized(metadata), all=normalized(style)+" "+content;
        var styles=topic(message);
        if(!styles.isEmpty() && styles.stream().noneMatch(all::contains))return false;
        String[][] groups={
            {"do","red"},{"hong","pink"},{"kem","cream","ivory"},{"beige","be"},
            {"den","black"},{"trang","white"},{"nau","brown"},{"navy"},{"pastel"},{"xanh la","green"},{"xanh duong","blue"},{"tim","purple"},{"vang","yellow"},{"cam","orange"},
            {"so mi","shirt"},{"blazer"},{"jeans","denim"},{"chan vay","skirt"},
            {"vay lien","dress"},{"quan tay","trousers"},{"polo"},{"sneaker"},{"guoc","heels"}
        };
        boolean specific=false;
        for(var group:groups) {
            boolean requested=false, matched=false;
            for(String term:group) {
                // 'do' and 'be' are ambiguous Vietnamese words; require a color prefix.
                String needle=(term.equals("do")||term.equals("be"))?"mau "+term:term;
                if((" "+query+" ").contains(" "+needle+" "))requested=true;
                if((" "+content.replaceAll("[^a-z0-9 ]"," ")+" ").contains(" "+term+" "))matched=true;
            }
            if(requested){specific=true;if(!matched)return false;}
        }
        if(specific)return true;
        if(styles.isEmpty())return false;
        // Broad style requests still need concrete clothing evidence in outfit metadata.
        String[] anchors={"so mi","shirt","polo","blazer","quan","trousers","jeans","vay","skirt","dress","knit","gile","len","loafer"};
        return Arrays.stream(anchors).anyMatch(content::contains);
    }
    static boolean reviewed(String provenance) {
        String value=normalized(provenance);
        if(value.contains("unreviewed") || value.contains("not reviewed") || value.contains("not inspected"))return false;
        return List.of("visually reviewed","individually reviewed","visually inspected","individually inspected",
            "visual review approved","reviewed existing asset").stream().anyMatch(value::contains);
    }
    private int score(JsonNode o,Set<String> terms){String s=normalized(o.toString());return (int)terms.stream().filter(t->s.contains(normalized(t))).count();}
    static String shortText(String s,int max){return s.substring(0,Math.min(max,s.length()));}
    static List<String> strings(JsonNode n,int max){var list=new ArrayList<String>();if(n.isArray())for(var x:n){if(list.size()==max)break;if(x.isTextual()&&!x.asText().isBlank())list.add(shortText(x.asText(),120));}return List.copyOf(list);}
}
