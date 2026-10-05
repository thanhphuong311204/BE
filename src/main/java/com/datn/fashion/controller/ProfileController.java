package com.datn.fashion.controller;
import com.datn.fashion.dto.profile.ProfileResponse;
import com.datn.fashion.dto.profile.UpdateProfileRequest;
import com.datn.fashion.service.ProfileService;
import com.datn.fashion.exception.ApiException;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
@RestController @RequestMapping("/api/profile")
public class ProfileController {
 private final ProfileService profiles;
 public ProfileController(ProfileService profiles) { this.profiles=profiles; }
 @GetMapping public ProfileResponse get(@AuthenticationPrincipal Jwt jwt) { return profiles.get(userId(jwt)); }
 @PutMapping public ProfileResponse update(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody UpdateProfileRequest r) { return profiles.update(userId(jwt),r); }
 @PostMapping(value="/avatar",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public ProfileResponse avatar(@AuthenticationPrincipal Jwt jwt,@RequestPart("file") MultipartFile file) { return profiles.upload(userId(jwt),file); }
 private Long userId(Jwt jwt) {
  try { long id=Long.parseLong(jwt.getSubject());if(id<=0) throw new NumberFormatException();return id; }
  catch(NumberFormatException e) { throw new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Token không hợp lệ."); }
 }
}
