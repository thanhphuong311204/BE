package com.datn.fashion.service;
import com.datn.fashion.dto.profile.ProfileResponse;
import com.datn.fashion.dto.profile.UpdateProfileRequest;
import com.datn.fashion.entity.User;
import com.datn.fashion.entity.UserProfile;
import com.datn.fashion.repository.UserProfileRepository;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;
@Service
public class ProfileService {
 private final UserRepository users;
 private final AvatarStorage avatars;
 private final UserProfileRepository profiles;
 public ProfileService(UserRepository users,UserProfileRepository profiles,AvatarStorage avatars) { this.users=users;this.profiles=profiles;this.avatars=avatars; }
 @Transactional(readOnly=true) public ProfileResponse get(Long id) { var u=user(id); return ProfileResponse.from(u,profile(id)); }
 @Transactional public ProfileResponse update(Long id,UpdateProfileRequest r) {
  var u=userForUpdate(id);var p=profile(id);String phone=Identifiers.phone(r.phoneNumber());
  if(users.existsByPhoneNumberAndIdNot(phone,id)) throw new ApiException(HttpStatus.CONFLICT,"PHONE_EXISTS","Số điện thoại đã tồn tại.",Map.of("phoneNumber","Số điện thoại đã tồn tại."));
  p.fullName=r.fullName().trim();u.phoneNumber=phone;
  users.saveAndFlush(u);profiles.saveAndFlush(p);return ProfileResponse.from(u,p);
 }
 @Transactional public ProfileResponse upload(Long id,MultipartFile file) {
  var u=userForUpdate(id);var p=profile(id);
  if(file.isEmpty()) throw invalid("Vui lòng chọn ảnh.");
  if(file.getSize()>5*1024*1024) throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE,"FILE_TOO_LARGE","Ảnh cần nhỏ hơn hoặc bằng 5 MB.");
  byte[] bytes;String extension;
  try {
   bytes=file.getBytes();
   // Decode actual image contents; do not trust extension or client Content-Type.
   try(var input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
    var readers=ImageIO.getImageReaders(input);
    if(!readers.hasNext()) throw invalid("Chỉ chấp nhận ảnh JPEG hoặc PNG.");
    var reader=readers.next();
    try {
     String format=reader.getFormatName().toLowerCase(Locale.ROOT);
     if(!Set.of("jpeg","jpg","png").contains(format)) throw invalid("Chỉ chấp nhận ảnh JPEG hoặc PNG.");
     reader.setInput(input,true,true);
     if(reader.getWidth(0)>4096 || reader.getHeight(0)>4096) throw invalid("Ảnh không được lớn hơn 4096 × 4096 pixel.");
     if(reader.read(0)==null) throw invalid("Ảnh bị lỗi hoặc không hợp lệ.");
     extension=format.equals("png") ? "png" : "jpg";
    } finally { reader.dispose(); }
   }
  } catch(IOException e) { throw invalid("Không đọc được ảnh. Vui lòng chọn ảnh khác."); }
  var uploaded=avatars.upload(bytes,"avatar."+extension,id);
  // Only the new asset ID is held in memory; the schema stores secure_url only.
  // On rollback, clean up the newly uploaded asset. Old assets require separate maintenance.
  TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
   @Override public void afterCompletion(int status) {
    String cleanup=status==STATUS_COMMITTED ? null : uploaded.publicId();
    if(cleanup!=null) avatars.delete(cleanup);
   }
  });
  if(uploaded.secureUrl()==null || uploaded.secureUrl().length()>1000) throw invalid("URL avatar không hợp lệ hoặc vượt quá 1000 ký tự.");
  p.avatarUrl=uploaded.secureUrl();
  users.saveAndFlush(u);profiles.saveAndFlush(p);return ProfileResponse.from(u,p);
 }
 private User userForUpdate(Long id) { return users.findLockedById(id).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Tài khoản không còn khả dụng. Vui lòng đăng nhập lại.")); }
 private User user(Long id) { return users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Tài khoản không còn khả dụng. Vui lòng đăng nhập lại.")); }
 private UserProfile profile(Long id) { return profiles.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,"PROFILE_NOT_FOUND","Không tìm thấy hồ sơ của tài khoản.")); }
 private ApiException invalid(String message) { return new ApiException(HttpStatus.BAD_REQUEST,"INVALID_IMAGE",message); }
}
