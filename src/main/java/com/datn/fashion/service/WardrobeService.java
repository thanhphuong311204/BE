package com.datn.fashion.service;
import com.datn.fashion.dto.wardrobe.*;
import com.datn.fashion.entity.User;
import com.datn.fashion.repository.*;
import com.datn.fashion.exception.ApiException;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
@Service
@Transactional(readOnly=true)
public class WardrobeService {
 private final WardrobeRepository wardrobe; private final ExploreCatalogRepository catalog;
 private final UserRepository users; private final CloudinaryImageStorage storage;
 private final ImageUploadValidator images; private final Validator validator;
 public WardrobeService(WardrobeRepository wardrobe,ExploreCatalogRepository catalog,UserRepository users,
  CloudinaryImageStorage storage,ImageUploadValidator images,Validator validator){
  this.wardrobe=wardrobe;this.catalog=catalog;this.users=users;this.storage=storage;this.images=images;this.validator=validator;
 }
 private void cleanup(String publicId) { try { storage.delete(publicId); } catch (RuntimeException ex) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("Wardrobe image cleanup failed: {}",ex.getClass().getSimpleName()); } }
 private void account(Long id) {
  users.findById(id).filter(u->u.status==User.Status.ACTIVE && u.deletedAt==null)
   .orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Tài khoản không còn khả dụng."));
 }
 public WardrobeOptionsResponse options(Long owner) {account(owner);return new WardrobeOptionsResponse(wardrobe.categories(),wardrobe.colors(),catalog.styles());}
 public List<WardrobeItemResponse> list(Long owner,Long main,Long sub,Long color) {account(owner);return wardrobe.list(owner,main,sub,color,null);}
 public WardrobeItemResponse detail(Long owner,Long id) {
  account(owner);return wardrobe.list(owner,null,null,null,id).stream().findFirst()
   .orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"WARDROBE_ITEM_NOT_FOUND","Không tìm thấy trang phục."));
 }
 private void validate(WardrobeItemRequest data) {
  var errors=new LinkedHashMap<String,String>();
  for(var e:validator.validate(data)) errors.put(e.getPropertyPath().toString(),e.getMessage());
  if(!errors.isEmpty())throw new ApiException(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Kiểm tra thông tin trang phục.",errors);
  var categories=wardrobe.categories();
  if(categories.stream().noneMatch(c->c.id().equals(data.mainCategoryId()) && c.parentId()==null))errors.put("mainCategoryId","Danh mục chính không hợp lệ.");
  if(data.subCategoryId()!=null && categories.stream().noneMatch(c->c.id().equals(data.subCategoryId()) && Objects.equals(c.parentId(),data.mainCategoryId())))
   errors.put("subCategoryId","Danh mục con không thuộc danh mục chính.");
  if(wardrobe.colors().stream().noneMatch(c->c.id().equals(data.colorId())))errors.put("colorId","Màu sắc không khả dụng.");
  var styles=data.styleIds()==null?List.<Long>of():data.styleIds();var active=new HashSet<Long>();
  catalog.styles().forEach(s->active.add(s.id()));
  if(styles.size()!=new HashSet<>(styles).size() || !active.containsAll(styles))errors.put("styleIds","Phong cách trùng lặp hoặc không khả dụng.");
  if(!errors.isEmpty())throw new ApiException(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Kiểm tra thông tin trang phục.",errors);
 }
 @Transactional public WardrobeItemResponse create(Long owner,WardrobeItemRequest data,MultipartFile file) {
  account(owner);validate(data);var image=images.validate(file);
  var upload=storage.upload(image.bytes(),image.filename(),ImageUploadTarget.WARDROBE,owner);
  TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
   @Override public void afterCompletion(int status){if(status!=STATUS_COMMITTED)cleanup(upload.publicId());}
  });
  try { Long id=wardrobe.create(owner,data,upload.secureUrl(),upload.publicId());return detail(owner,id); } catch (org.springframework.dao.DataAccessException e) { var error=new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,"WARDROBE_SAVE_FAILED","Không thể lưu trang phục. Vui lòng thử lại.");error.initCause(e);throw error; }
 }
 @Transactional public WardrobeItemResponse update(Long owner,Long id,WardrobeItemRequest data) {
  detail(owner,id);validate(data);wardrobe.update(owner,id,data);return detail(owner,id);
 }
 @Transactional public void delete(Long owner,Long id) {
  detail(owner,id);String publicId=wardrobe.publicId(owner,id);wardrobe.delete(owner,id);
  TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
   @Override public void afterCommit(){cleanup(publicId);}
  });
 }
}
