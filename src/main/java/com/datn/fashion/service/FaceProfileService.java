package com.datn.fashion.service;
import com.datn.fashion.entity.*;
import com.datn.fashion.entity.FaceShapeHistory.*;
import com.datn.fashion.repository.*;
import com.datn.fashion.exception.ApiException;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import java.time.Instant;
import java.util.*;
@Service
public class FaceProfileService {
 public record Confirmation(@NotNull Shape confirmedFaceShape,@NotNull Source confirmationSource,Shape expectedFaceShape,Long expectedUserId){}
 public record Profile(Long userId,Shape confirmedFaceShape,Source confirmationSource,Instant confirmedAt,Instant updatedAt){}
 public record Change(Long id,Shape previousFaceShape,Shape confirmedFaceShape,Source confirmationSource,Instant changedAt){}
 public record History(List<Change> items,Long nextCursor){}
 private final UserRepository users;private final UserProfileRepository profiles;private final FaceShapeHistoryRepository history;
 public FaceProfileService(UserRepository users,UserProfileRepository profiles,FaceShapeHistoryRepository history){this.users=users;this.profiles=profiles;this.history=history;}
 private User active(Long id,boolean lock){var user=(lock?users.findLockedById(id):users.findById(id)).orElseThrow(this::unauthorized);if(user.status!=User.Status.ACTIVE||user.deletedAt!=null)throw unauthorized();return user;}
 private ApiException unauthorized(){return new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Tài khoản không còn khả dụng.");}
 private UserProfile profile(Long id){return profiles.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"PROFILE_NOT_FOUND","Không tìm thấy hồ sơ tài khoản."));}
 private Profile dto(UserProfile p){return new Profile(p.userId,p.confirmedFaceShape,p.faceConfirmationSource,p.faceConfirmedAt,p.faceShapeUpdatedAt);}
 @Transactional(readOnly=true) public Profile get(Long id){active(id,false);return dto(profile(id));}
 @Transactional public Profile confirm(Long id,Confirmation request){
  active(id,true);var p=profile(id);
  if(request.expectedUserId()!=null&&!request.expectedUserId().equals(id))throw new ApiException(HttpStatus.CONFLICT,"ACCOUNT_CHANGED","Tài khoản đã thay đổi. Vui lòng mở lại Beauty.");
  // Repeated taps/retries of the same confirmed shape never append duplicate history.
  if(p.confirmedFaceShape==request.confirmedFaceShape())return dto(p);
  if(p.confirmedFaceShape!=request.expectedFaceShape())throw new ApiException(HttpStatus.CONFLICT,"FACE_PROFILE_CHANGED","Dáng mặt đã thay đổi ở phiên khác. Hãy tải lại trước khi xác nhận.");
  var now=Instant.now();var row=new FaceShapeHistory();row.userId=id;row.previousShape=p.confirmedFaceShape;row.confirmedShape=request.confirmedFaceShape();row.confirmationSource=request.confirmationSource();row.changedAt=now;
  p.confirmedFaceShape=request.confirmedFaceShape();p.faceConfirmationSource=request.confirmationSource();p.faceConfirmedAt=now;p.faceShapeUpdatedAt=now;
  profiles.saveAndFlush(p);history.saveAndFlush(row);return dto(p);
 }
 @Transactional(readOnly=true) public History history(Long id,long beforeId){active(id,false);if(beforeId<1)throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_CURSOR","Cursor không hợp lệ.");var rows=history.findByUserIdAndIdLessThanOrderByIdDesc(id,beforeId,PageRequest.of(0,51));var page=rows.stream().limit(50).map(r->new Change(r.id,r.previousShape,r.confirmedShape,r.confirmationSource,r.changedAt)).toList();return new History(page,rows.size()>50?page.get(page.size()-1).id():null);}
}
