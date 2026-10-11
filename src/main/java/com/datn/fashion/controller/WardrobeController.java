package com.datn.fashion.controller;
import com.datn.fashion.dto.wardrobe.*;
import com.datn.fashion.service.WardrobeService;
import com.datn.fashion.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
@RestController @RequestMapping("/api/wardrobe")
public class WardrobeController {
 private final WardrobeService service;private final ObjectMapper json;
 public WardrobeController(WardrobeService service,ObjectMapper json){this.service=service;this.json=json;}
 private Long owner(Jwt jwt){try{long id=Long.parseLong(jwt.getSubject());if(id<=0)throw new NumberFormatException();return id;}catch(NumberFormatException e){throw new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Token không hợp lệ.");}}
 @GetMapping("/options") public WardrobeOptionsResponse options(@AuthenticationPrincipal Jwt jwt){return service.options(owner(jwt));}
 @GetMapping("/items") public Map<String,Object> list(@AuthenticationPrincipal Jwt jwt,@RequestParam(required=false) Long mainCategoryId,
  @RequestParam(required=false) Long subCategoryId,@RequestParam(required=false) Long colorId){return Map.of("items",service.list(owner(jwt),mainCategoryId,subCategoryId,colorId));}
 @GetMapping("/items/{id}") public WardrobeItemResponse detail(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id){return service.detail(owner(jwt),id);}
 @PostMapping(value="/items",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
 public WardrobeItemResponse create(@AuthenticationPrincipal Jwt jwt,@RequestParam("item") String item,@RequestPart("file") MultipartFile file){
  try {var data=json.readValue(item,WardrobeItemRequest.class);if(data==null)throw new ApiException(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Thiếu thông tin trang phục.");return service.create(owner(jwt),data,file);}
  catch(JsonProcessingException e){throw new ApiException(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Thông tin trang phục không đúng định dạng JSON.");}
 }
 @PutMapping("/items/{id}") public WardrobeItemResponse update(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id,@Valid @RequestBody WardrobeItemRequest data){return service.update(owner(jwt),id,data);}
 @DeleteMapping("/items/{id}") public Map<String,String> delete(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id){service.delete(owner(jwt),id);return Map.of("message","Đã xóa trang phục.");}
}
