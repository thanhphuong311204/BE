package com.datn.fashion.service;

import com.datn.fashion.dto.face.FaceAnalysisResponse.Shape;
import com.datn.fashion.entity.User;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.util.*;

/** Read-only editorial rules. Does not invoke Python, Gemini, image analysis or persistence. */
@Service
public class BeautyCatalogService {
    public enum Type { HAIRSTYLE, MAKEUP }
    public enum Scope { RECOMMENDED, ALL }
    public record ImageRef(String url,String sourceUrl,String rightsBasis,String evidenceRef,String reviewerId,String caption,String attribution) {}
    public record Swatch(String name,String hex) {}
    public record GuideSection(String title,String text) {}
    public record MakeupPalette(String role,List<Swatch> colors,Map<String,List<Swatch>> alternatives) {}
    public record LibraryMeta(String slug,String explanation,List<String> occasions,List<String> seasons,String difficulty,List<String> tags,String hairLength,String hairTexture,List<String> care,List<Swatch> palette,List<String> relatedIds,List<String> hairLengths,List<String> hairTypes,List<String> hairColors,List<GuideSection> makeupSteps,List<MakeupPalette> makeupPalette,String makeupCategory) {}
    public record Related(String id,Type type,String name,ImageRef thumbnail) {}
    public record Content(String id,Type type,String name,String description,List<Shape> suitableFaceShapes,
        Map<Shape,String> reasons,ImageRef thumbnail,List<ImageRef> referenceImages,String imageStatus,
        List<String> steps,List<String> tools,List<String> colorSuggestions,String editorialStatus,LibraryMeta library) {}
    public record Catalog(String version,String mechanism,String disclaimer,List<Content> items) {}
    public record Item(String id,Type type,String name,String description,List<Shape> suitableFaceShapes,
        String reason,boolean matchesConfirmedShape,ImageRef thumbnail,List<ImageRef> referenceImages,String imageStatus,
        List<String> steps,List<String> tools,List<String> colorSuggestions,String editorialStatus,LibraryMeta library,List<Related> related) {}
    public record Listing(String version,String mechanism,String disclaimer,Shape confirmedFaceShape,Scope scope,List<Item> items,int page,int size,long total,boolean hasNext) {}
    public record Detail(String version,String mechanism,String disclaimer,Shape confirmedFaceShape,Item item) {}
    private final UserRepository users;
    private final com.datn.fashion.repository.UserProfileRepository profiles;
    private final Catalog catalog;
    private final Map<String,Content> index;
    public BeautyCatalogService(UserRepository users,com.datn.fashion.repository.UserProfileRepository profiles,ObjectMapper mapper,ResourceLoader loader,
        @Value("${BEAUTY_CATALOG_PATH:classpath:beauty-catalog.json}") String resource,
        @Value("${app.cloudinary.cloud-name:}") String cloud) throws java.io.IOException {
        this.users=users;this.profiles=profiles;
        try(var in=loader.getResource(resource).getInputStream()){catalog=mapper.readValue(in,Catalog.class);}
        if(catalog.items()==null||catalog.items().size()>100||blank(catalog.version())||blank(catalog.disclaimer())||!"FACE_SHAPE_RULE_BASED".equals(catalog.mechanism()))throw new IllegalStateException("Invalid beauty catalog metadata");
        var map=new LinkedHashMap<String,Content>();
        for(var c:catalog.items()) {
            if(c.id()==null||!c.id().matches("[a-z0-9-]{1,80}")||map.put(c.id(),c)!=null||c.type()==null||blank(c.name())||blank(c.description())
                ||c.suitableFaceShapes()==null||c.suitableFaceShapes().isEmpty()||c.reasons()==null||c.steps()==null||c.steps().isEmpty()
                ||c.tools()==null||c.colorSuggestions()==null||c.referenceImages()==null||blank(c.editorialStatus()))throw new IllegalStateException("Invalid beauty item");
            for(var shape:c.suitableFaceShapes())if(blank(c.reasons().get(shape)))throw new IllegalStateException("Missing shape explanation");
            if(c.steps().stream().anyMatch(BeautyCatalogService::blank))throw new IllegalStateException("Empty beauty step");
            if(c.thumbnail()!=null)validateImage(c.thumbnail(),cloud);
            for(var image:c.referenceImages())validateImage(image,cloud);
        }
        index=Collections.unmodifiableMap(map);
        var slugs=new HashSet<String>();
        for(var c:catalog.items()) {
            var m=c.library();
            if(c.type()==Type.MAKEUP && m!=null && m.makeupCategory()!=null && !Set.of("LOOK","TECHNIQUE").contains(m.makeupCategory()))throw new IllegalStateException("Invalid makeup category");
            if(m==null||blank(m.slug())||!m.slug().matches("[a-z0-9-]{1,80}")||!slugs.add(m.slug())||blank(m.explanation())||m.occasions()==null||m.occasions().isEmpty()||m.seasons()==null||m.seasons().isEmpty()||!Set.of("EASY","MEDIUM","ADVANCED").contains(m.difficulty())||m.palette()==null||m.tags()==null||m.care()==null||m.relatedIds()==null)throw new IllegalStateException("Invalid library metadata");
            for(var sw:m.palette())if(blank(sw.name())||sw.hex()==null||!sw.hex().matches("#[0-9a-fA-F]{6}"))throw new IllegalStateException("Invalid palette");
            for(var id:m.relatedIds())if(!map.containsKey(id)||map.get(id).type()==c.type())throw new IllegalStateException("Invalid related content");
        }
    }
    private static boolean blank(String s){return s==null||s.isBlank();}
    private void validateImage(ImageRef image,String cloud) {
        try {
            var u=URI.create(image.url());var source=URI.create(image.sourceUrl());
            if(cloud.isBlank()||!"https".equals(u.getScheme())||!"res.cloudinary.com".equals(u.getHost())||u.getUserInfo()!=null||u.getQuery()!=null||u.getFragment()!=null||u.getPort()!=-1||!u.getPath().startsWith("/"+cloud+"/image/upload/")||u.getPath().contains("..")
                ||!"https".equals(source.getScheme())||source.getHost()==null||source.getUserInfo()!=null||blank(image.rightsBasis())||!image.evidenceRef().matches("sha256:[a-f0-9]{64}")||blank(image.reviewerId())||blank(image.caption())||blank(image.attribution()))throw new IllegalArgumentException();
        } catch(Exception e){throw new IllegalStateException("Beauty image requires verified source/rights and configured Cloudinary delivery");}
    }
    public void account(Long id){users.findById(id).filter(u->u.status==User.Status.ACTIVE&&u.deletedAt==null).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Tài khoản không còn khả dụng."));}
    public Listing list(Long owner,Shape shape,Type type,Scope scope) {
        account(owner);
        var items=catalog.items().stream().filter(c->type==null||c.type()==type).filter(c->scope==Scope.ALL||c.suitableFaceShapes().contains(shape)).map(c->item(c,shape)).toList();
        return new Listing(catalog.version(),catalog.mechanism(),catalog.disclaimer(),shape,scope,items,0,items.size(),items.size(),false);
    }
    public Detail detail(Long owner,String id,Shape shape) {
        account(owner);var content=index.get(id);
        if(content==null)throw new ApiException(HttpStatus.NOT_FOUND,"BEAUTY_NOT_FOUND","Không tìm thấy gợi ý này.");
        return new Detail(catalog.version(),catalog.mechanism(),catalog.disclaimer(),shape,item(content,shape));
    }
    private Item item(Content c,Shape shape) {
        boolean match=shape!=null&&c.suitableFaceShapes().contains(shape);
        return new Item(c.id(),c.type(),c.name(),c.description(),c.suitableFaceShapes(),match?c.reasons().get(shape):(shape==null?"Bạn chưa lưu dáng mặt. Đây là nội dung khám phá theo sở thích, chưa cá nhân hóa theo dáng mặt.":"Lựa chọn khám phá ngoài bộ lọc dáng mặt đã chọn. Bạn có thể thử theo sở thích, kết cấu tóc và điều kiện thực tế."),match,c.thumbnail(),c.referenceImages(),c.thumbnail()==null?"MISSING_LICENSED_IMAGE":"AVAILABLE",c.steps(),c.tools(),c.colorSuggestions(),c.editorialStatus(),c.library(),c.library().relatedIds().stream().map(index::get).map(r->new Related(r.id(),r.type(),r.name(),r.thumbnail())).toList());
    }
    private String normalize(String s) {return java.text.Normalizer.normalize(s,java.text.Normalizer.Form.NFD).replaceAll("\\p{M}","").replace('đ','d').replace('Đ','D').toLowerCase(Locale.ROOT);}
    private ApiException invalid(){return new ApiException(HttpStatus.BAD_REQUEST,"INVALID_BEAUTY_FILTER","Bộ lọc hoặc phân trang không hợp lệ.");}
    public Shape savedShape(Long owner){return profiles.findById(owner).map(p->p.confirmedFaceShape==null?null:Shape.valueOf(p.confirmedFaceShape.name())).orElse(null);}
    public Listing library(Long owner,Type type,Shape filter,String q,String season,String occasion,String difficulty,String length,String hairType,String hairColor,int page,int size,Scope scope) {
        return library(owner,type,filter,q,season,occasion,difficulty,length,hairType,hairColor,page,size,scope,null);
    }
    public Listing library(Long owner,Type type,Shape filter,String q,String season,String occasion,String difficulty,String length,String hairType,String hairColor,int page,int size,Scope scope,String makeupCategory) {
        account(owner);
        if(makeupCategory!=null && (!Set.of("LOOK","TECHNIQUE").contains(makeupCategory)||type!=Type.MAKEUP))throw invalid();
        if(page<0||page>1000||size<1||size>50||q.length()>100)throw invalid();
        if(hairType!=null&&!Set.of("STRAIGHT","WAVY","CURLY","LAYERED","BANGS","BRAIDED","UPDO","HALF_UP").contains(hairType)||hairColor!=null&&!Set.of("BLACK","BROWN","CARAMEL").contains(hairColor))throw invalid();
        if(season!=null&&!Set.of("SPRING","SUMMER","AUTUMN","WINTER","ALL_YEAR").contains(season)||occasion!=null&&!Set.of("DAILY","WORK","DATE","PARTY","WEDDING","OUTDOOR","FESTIVAL").contains(occasion)||difficulty!=null&&!Set.of("EASY","MEDIUM","ADVANCED").contains(difficulty)||length!=null&&!Set.of("SHORT","MEDIUM","LONG","ANY").contains(length))throw invalid();
        var saved=savedShape(owner);String search=normalize(q.trim());
        var all=catalog.items().stream().filter(c->type==null||c.type()==type)
            .filter(c->makeupCategory==null||makeupCategory.equals(c.library().makeupCategory()))
            .filter(c->filter==null||c.suitableFaceShapes().contains(filter))
            .filter(c->scope==Scope.ALL||saved==null||c.suitableFaceShapes().contains(saved))
            .filter(c->search.isEmpty()||normalize(c.name()+" "+c.description()+" "+String.join(" ",c.library().tags())).contains(search))
            .filter(c->season==null||c.library().seasons().contains(season)||c.library().seasons().contains("ALL_YEAR"))
            .filter(c->occasion==null||c.library().occasions().contains(occasion))
            .filter(c->difficulty==null||difficulty.equals(c.library().difficulty()))
            .filter(c->length==null||"ANY".equals(length)||c.library().hairLengths()!=null&&c.library().hairLengths().contains(length)||c.library().hairLengths()==null&&(length.equals(c.library().hairLength())||"ANY".equals(c.library().hairLength())))
            .filter(c->hairType==null||c.library().hairTypes()!=null&&c.library().hairTypes().contains(hairType))
            .filter(c->hairColor==null||c.library().hairColors()!=null&&c.library().hairColors().contains(hairColor))
            .sorted(Comparator.comparingInt((Content c)->saved!=null&&c.suitableFaceShapes().contains(saved)?0:1)).toList();
        long start=(long)page*size;var result=all.stream().skip(start).limit(size).map(c->item(c,saved)).toList();
        return new Listing(catalog.version(),catalog.mechanism(),catalog.disclaimer(),saved,scope,result,page,size,all.size(),start+result.size()<all.size());
    }
    public Detail libraryDetail(Long owner,String id){account(owner);return detail(owner,id,savedShape(owner));}

}
