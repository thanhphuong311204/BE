package com.datn.fashion.controller;

import com.datn.fashion.dto.beauty.BeautyTryOnResponse;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.service.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/beauty/try-on")
public class BeautyTryOnController {
    private static final Set<String> IDS=Set.of("makeup-lip-focus","makeup-soft-cheek","makeup-horizontal-blush","makeup-diagonal-blush","makeup-soft-eye","look-natural-daily","look-korean-soft-glam","look-douyin-inspired","look-clean-girl","look-peach","look-rose","look-soft-brown","look-evening-glam");
    private final BeautyPortraitSessions portraits;private final BeautyCatalogService catalog;private final ImageUploadValidator validator;private final BeautyTryOnClient client;
    public BeautyTryOnController(BeautyCatalogService catalog,ImageUploadValidator validator,BeautyTryOnClient client,BeautyPortraitSessions portraits){this.portraits=portraits;this.catalog=catalog;this.validator=validator;this.client=client;}
    private void account(Jwt jwt){try{catalog.account(Long.parseLong(jwt.getSubject()));}catch(NumberFormatException e){throw new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Token không hợp lệ.");}}
    @GetMapping("/capabilities") public ResponseEntity<Map<?,?>> capabilities(@AuthenticationPrincipal Jwt jwt){account(jwt);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(client.capabilities());}
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BeautyTryOnResponse> render(@AuthenticationPrincipal Jwt jwt,@RequestPart("file")MultipartFile file,
        @RequestParam String recommendationId,@RequestParam(defaultValue="0.5")double intensity,@RequestParam String color,
        @RequestParam(defaultValue="0")double offsetX,@RequestParam(defaultValue="0")double offsetY,@RequestParam(defaultValue="1")double scale) {
        account(jwt);
        if(!IDS.contains(recommendationId)||color==null||!color.matches("#[0-9a-fA-F]{6}")||!Double.isFinite(intensity)||intensity<0||intensity>1
            ||!Double.isFinite(offsetX)||Math.abs(offsetX)>.3||!Double.isFinite(offsetY)||Math.abs(offsetY)>.3||!Double.isFinite(scale)||scale<.7||scale>1.5)
            throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_TRYON_OPTIONS","Tùy chọn thử không hợp lệ hoặc kiểu chưa được hỗ trợ.");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(client.render(validator.validate(file),recommendationId,intensity,color,offsetX,offsetY,scale));
    }

    @PostMapping(value="/portrait",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BeautyTryOnResponse> portrait(@AuthenticationPrincipal Jwt jwt,@RequestPart("file")MultipartFile file) {
        account(jwt);var image=validator.validate(file);
        var result=client.render(image,"makeup-lip-focus",0,"#AD3456",0,0,1);
        if("SUCCESS".equals(result.status()))result=result.withToken(portraits.put(jwt.getSubject(),image));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }
    public record SessionRequest(String portraitToken,String recommendationId,double intensity,String color) {
        @Override public String toString(){return "SessionRequest[REDACTED]";}
    }
    @PostMapping(value="/session/render",consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BeautyTryOnResponse> session(@AuthenticationPrincipal Jwt jwt,@RequestBody SessionRequest request) {
        account(jwt);
        if(request==null||!IDS.contains(request.recommendationId()==null?"":request.recommendationId())||!Double.isFinite(request.intensity())||request.intensity()<0||request.intensity()>1||request.color()==null||!request.color().matches("#[0-9a-fA-F]{6}"))
            throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_TRYON_OPTIONS","Tùy chọn thử không hợp lệ.");
        var image=portraits.get(jwt.getSubject(),request.portraitToken());
        try {return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(client.render(image,request.recommendationId(),request.intensity(),request.color(),0,0,1));}
        finally {java.util.Arrays.fill(image.bytes(),(byte)0);}
    }
    @DeleteMapping("/portrait")
    public ResponseEntity<Void> release(@AuthenticationPrincipal Jwt jwt,@RequestBody SessionRequest request){
        account(jwt);portraits.remove(jwt.getSubject(),request.portraitToken());return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
