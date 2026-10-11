package com.datn.fashion.controller;
import com.datn.fashion.dto.explore.*;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.service.ExploreCatalogService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/explore/styles")
public class ExploreCatalogController {
 private final ExploreCatalogService service;
 public ExploreCatalogController(ExploreCatalogService service) { this.service=service; }
 @GetMapping public ExploreCatalogResponse list(@AuthenticationPrincipal Jwt jwt) {
  return service.list(userId(jwt));
 }
 @GetMapping("/by-id/{id}") public ExploreStyleDetailResponse detailById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) { return service.detailById(userId(jwt),id); }
 @GetMapping("/{slug}") public ExploreStyleDetailResponse detail(@AuthenticationPrincipal Jwt jwt, @PathVariable String slug) {
  return service.detail(userId(jwt),slug);
 }
 private Long userId(Jwt jwt) {
  try { long id=Long.parseLong(jwt.getSubject()); if(id <= 0) throw new NumberFormatException(); return id; }
  catch (NumberFormatException ex) { throw new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Token không hợp lệ."); }
 }
}
