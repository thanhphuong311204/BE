package com.datn.fashion.controller;
import com.datn.fashion.dto.style.*;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.service.StylePreferencesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/style-preferences")
public class StylePreferencesController {
    private final StylePreferencesService service;
    public StylePreferencesController(StylePreferencesService service) { this.service = service; }
    @GetMapping("/options")
    public StyleOptionsResponse options(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "vi") String language) {
        return service.options(userId(jwt), language);
    }
    @GetMapping public StylePreferencesResponse get(@AuthenticationPrincipal Jwt jwt) { return service.get(userId(jwt)); }
    @PutMapping public StylePreferencesResponse save(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveStylePreferencesRequest request) {
        return service.save(userId(jwt), request);
    }
    @PostMapping("/skip") public StylePreferencesResponse skip(@AuthenticationPrincipal Jwt jwt) { return service.skip(userId(jwt)); }
    private Long userId(Jwt jwt) {
        try { long id = Long.parseLong(jwt.getSubject()); if (id <= 0) throw new NumberFormatException(); return id; }
        catch (NumberFormatException e) { throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Token không hợp lệ."); }
    }
}
