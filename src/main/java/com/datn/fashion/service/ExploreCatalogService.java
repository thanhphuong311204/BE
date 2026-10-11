package com.datn.fashion.service;
import com.datn.fashion.dto.explore.*;
import com.datn.fashion.entity.User;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

@Service
@Transactional(readOnly = true)
public class ExploreCatalogService {
 private final ExploreCatalogRepository catalog;
 private final UserRepository users; private final StyleGuideRepository guides;
 public ExploreCatalogService(ExploreCatalogRepository catalog, UserRepository users, StyleGuideRepository guides) {
  this.catalog=catalog; this.users=users; this.guides=guides;
 }
 private void account(Long id) {
  users.findById(id).filter(u -> u.status == User.Status.ACTIVE && u.deletedAt == null)
   .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Tài khoản không còn khả dụng."));
 }
 public ExploreCatalogResponse list(Long id) {
  account(id); return new ExploreCatalogResponse(catalog.styles(), catalog.groups());
 }
 public ExploreStyleDetailResponse detailById(Long userId, Long id) { account(userId); var style=catalog.detailById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,"STYLE_NOT_FOUND","Không tìm thấy phong cách.")); return new ExploreStyleDetailResponse(style,guides.get(style.id()).orElse(null)); }
 public ExploreStyleDetailResponse detail(Long id, String slug) {
  account(id);
  var style = catalog.detail(slug).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
   "STYLE_NOT_FOUND","Phong cách không tồn tại hoặc đã ngừng hoạt động.")); return new ExploreStyleDetailResponse(style, guides.get(style.id()).orElse(null));
 }
}
