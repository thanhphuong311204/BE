package com.datn.fashion.controller;
import com.datn.fashion.dto.auth.RegisterRequest;
import com.datn.fashion.dto.auth.LoginRequest;
import com.datn.fashion.dto.auth.LoginResponse;
import com.datn.fashion.dto.profile.ProfileResponse;
import com.datn.fashion.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final AuthService auth;
 public AuthController(AuthService auth) { this.auth=auth; }
 @PostMapping("/register") public ResponseEntity<ProfileResponse> register(@Valid @RequestBody RegisterRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(r)); }
 @PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginRequest r) { return auth.login(r); }
}
