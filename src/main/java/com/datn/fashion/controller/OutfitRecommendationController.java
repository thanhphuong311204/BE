package com.datn.fashion.controller;
import com.datn.fashion.dto.wardrobe.OutfitRecommendationsResponse;
import com.datn.fashion.service.OutfitRecommendationService;
import com.datn.fashion.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/wardrobe/outfit-recommendations")
public class OutfitRecommendationController {
 private final OutfitRecommendationService service;
 public OutfitRecommendationController(OutfitRecommendationService service) {this.service=service;}
 @GetMapping public OutfitRecommendationsResponse get(@AuthenticationPrincipal Jwt jwt,
   @RequestParam(defaultValue="0") String page,@RequestParam(defaultValue="6") String limit) {
  long owner;
  try {owner=Long.parseLong(jwt.getSubject());if(owner<=0)throw new NumberFormatException();}
  catch(NumberFormatException e) {throw new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Token không hợp lệ.");}
  return service.recommend(owner,number(page),number(limit));
 }
 private int number(String value) {
  try {return Integer.parseInt(value);}
  catch(NumberFormatException e) {throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_PAGINATION","Tham số phân trang phải là số nguyên hợp lệ.");}
 }
}
