package com.datn.fashion.controller;

import com.datn.fashion.dto.face.FaceAnalysisResponse.Shape;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.service.BeautyCatalogService;
import com.datn.fashion.service.BeautyCatalogService.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/beauty/recommendations")
public class BeautyRecommendationController {
    private final BeautyCatalogService service;
    public BeautyRecommendationController(BeautyCatalogService service){this.service=service;}
    @GetMapping public ResponseEntity<Listing> list(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="") String faceShape,
        @RequestParam(required=false) String type,@RequestParam(defaultValue="RECOMMENDED") String scope,
        @RequestParam(defaultValue="false") boolean library,@RequestParam(defaultValue="") String q,
        @RequestParam(required=false) String season,@RequestParam(required=false) String occasion,@RequestParam(required=false) String difficulty,@RequestParam(required=false) String hairLength,@RequestParam(required=false) String hairType,@RequestParam(required=false) String hairColor,
        @RequestParam(required=false) String makeupCategory,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        if(library)return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.library(owner(jwt),type==null?null:value(Type.class,type),faceShape.isBlank()?null:value(Shape.class,faceShape),q,season,occasion,difficulty,hairLength,hairType,hairColor,page,size,value(Scope.class,scope),makeupCategory));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.list(owner(jwt),value(Shape.class,faceShape),type==null?null:value(Type.class,type),value(Scope.class,scope)));
    }
    @GetMapping("/{id}") public ResponseEntity<Detail> detail(@AuthenticationPrincipal Jwt jwt,@PathVariable String id,@RequestParam(defaultValue="") String faceShape,@RequestParam(defaultValue="false") boolean library) {
        if(library)return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.libraryDetail(owner(jwt),id));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.detail(owner(jwt),id,value(Shape.class,faceShape)));
    }
    private Long owner(Jwt jwt){try{long id=Long.parseLong(jwt.getSubject());if(id<=0)throw new NumberFormatException();return id;}catch(NumberFormatException e){throw new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Token không hợp lệ.");}}
    private <T extends Enum<T>> T value(Class<T> type,String raw){try{return Enum.valueOf(type,raw);}catch(IllegalArgumentException e){throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_BEAUTY_FILTER","Dáng mặt hoặc bộ lọc gợi ý không hợp lệ.");}}
}
