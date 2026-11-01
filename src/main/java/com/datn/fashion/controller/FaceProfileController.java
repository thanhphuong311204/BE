package com.datn.fashion.controller;
import com.datn.fashion.service.FaceProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import com.datn.fashion.exception.ApiException;

@RestController @RequestMapping("/api/profile/face-shape")
public class FaceProfileController {
 private final FaceProfileService service;
 public FaceProfileController(FaceProfileService service){this.service=service;}
 private Long owner(Jwt jwt){try{long id=Long.parseLong(jwt.getSubject());if(id<=0)throw new NumberFormatException();return id;}catch(NumberFormatException e){throw new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Token không hợp lệ.");}}
 @GetMapping public ResponseEntity<FaceProfileService.Profile> get(@AuthenticationPrincipal Jwt jwt){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.get(owner(jwt)));}
 @PutMapping public ResponseEntity<FaceProfileService.Profile> confirm(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody FaceProfileService.Confirmation request){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.confirm(owner(jwt),request));}
 @GetMapping("/history") public ResponseEntity<FaceProfileService.History> history(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="9223372036854775807") long beforeId){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.history(owner(jwt),beforeId));}
}
