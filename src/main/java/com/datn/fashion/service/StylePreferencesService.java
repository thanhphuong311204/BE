package com.datn.fashion.service;

import com.datn.fashion.dto.style.*;
import com.datn.fashion.entity.User;
import com.datn.fashion.entity.UserStylePreferences;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.repository.*;
import static com.datn.fashion.repository.StyleOptionsRepository.Catalog.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class StylePreferencesService {
    private final UserRepository users;
    private final UserStylePreferencesRepository preferences;
    private final StyleOptionsRepository options;
    public StylePreferencesService(UserRepository users, UserStylePreferencesRepository preferences, StyleOptionsRepository options) {
        this.users = users; this.preferences = preferences; this.options = options;
    }

    @Transactional(readOnly = true)
    public StyleOptionsResponse options(Long userId, String language) {
        account(userId, false);
        if (!Set.of("vi", "en").contains(language))
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_LANGUAGE", "Chỉ hỗ trợ tiếng Việt hoặc tiếng Anh.");
        return new StyleOptionsResponse(options.options(STYLE, language), options.options(COLOR, language), options.options(OCCASION, language));
    }

    @Transactional(readOnly = true)
    public StylePreferencesResponse get(Long userId) {
        account(userId, false);
        return preferences.findById(userId).map(StylePreferencesResponse::from)
            .orElseGet(StylePreferencesResponse::notStarted);
    }

    @Transactional
    public StylePreferencesResponse save(Long userId, SaveStylePreferencesRequest request) {
        // Lock the account also when no preferences row exists, serializing save/skip
        // for this user without touching Auth/Profile implementation.
        account(userId, true);
        validate(STYLE, request.styleIds(), "styleIds");
        validate(COLOR, request.colorIds(), "colorIds");
        validate(OCCASION, request.occasionIds(), "occasionIds");
        var p = preferences.findById(userId).orElseGet(() -> create(userId));
        p.styleIds.clear(); p.styleIds.addAll(request.styleIds());
        p.colorIds.clear(); p.colorIds.addAll(request.colorIds());
        p.occasionIds.clear(); p.occasionIds.addAll(request.occasionIds());
        p.status = UserStylePreferences.Status.COMPLETED;
        p.completedAt = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        // Retain skippedAt as history when a previously skipped user completes.
        return StylePreferencesResponse.from(preferences.saveAndFlush(p));
    }

    @Transactional
    public StylePreferencesResponse skip(Long userId) {
        account(userId, true);
        var p = preferences.findById(userId).orElseGet(() -> create(userId));
        if (p.status != UserStylePreferences.Status.COMPLETED) {
            p.status = UserStylePreferences.Status.SKIPPED;
            if (p.skippedAt == null) p.skippedAt = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
            preferences.saveAndFlush(p);
        }
        return StylePreferencesResponse.from(p);
    }

    private UserStylePreferences create(Long id) {
        var p = new UserStylePreferences(); p.userId = id; return p;
    }
    private void validate(StyleOptionsRepository.Catalog catalog, List<Long> ids, String field) {
        if (new HashSet<>(ids).size() != ids.size())
            throw invalid(field, "Không chọn trùng một mục.");
        if (!options.allActive(catalog, ids))
            throw invalid(field, "Lựa chọn không tồn tại hoặc đã ngừng hoạt động. Vui lòng tải lại danh mục.");
    }
    private ApiException invalid(String field, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PREFERENCES", message, Map.of(field, message));
    }
    private User account(Long id, boolean lock) {
        var found = lock ? users.findLockedById(id) : users.findById(id);
        return found.filter(u -> u.status == User.Status.ACTIVE && u.deletedAt == null)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Tài khoản không còn khả dụng."));
    }
}
