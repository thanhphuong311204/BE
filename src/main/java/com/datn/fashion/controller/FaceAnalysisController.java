package com.datn.fashion.controller;

import com.datn.fashion.dto.face.FaceAnalysisResponse;
import com.datn.fashion.entity.User;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.service.FaceInferenceClient;
import com.datn.fashion.service.ImageUploadValidator;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai/face-analysis")
public class FaceAnalysisController {
    private final UserRepository users;
    private final ImageUploadValidator validator;
    private final FaceInferenceClient inference;
    public FaceAnalysisController(UserRepository users,ImageUploadValidator validator,FaceInferenceClient inference) {
        this.users=users;this.validator=validator;this.inference=inference;
    }
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FaceAnalysisResponse> analyze(@AuthenticationPrincipal Jwt jwt,@RequestPart("file") MultipartFile file) {
        long id;
        try { id=Long.parseLong(jwt.getSubject());if(id<=0)throw new NumberFormatException(); }
        catch (NumberFormatException e) { throw unauthorized(); }
        var user=users.findById(id).orElseThrow(this::unauthorized);
        if(user.status!=User.Status.ACTIVE || user.deletedAt!=null)throw unauthorized();
        // No image storage, Cloudinary call, result persistence, or model training.
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(inference.analyze(validator.validate(file)));
    }
    private ApiException unauthorized() { return new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Tài khoản không còn khả dụng. Vui lòng đăng nhập lại."); }
}
