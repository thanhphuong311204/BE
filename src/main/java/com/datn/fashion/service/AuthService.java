package com.datn.fashion.service;

import com.datn.fashion.dto.auth.*;
import com.datn.fashion.dto.profile.ProfileResponse;
import com.datn.fashion.entity.User;
import com.datn.fashion.entity.UserProfile;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.repository.UserProfileRepository;
import com.datn.fashion.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final PasswordEncoder passwords;
    private final JwtService tokens;
    private final PasswordChangeRateLimiter passwordChanges;

    public AuthService(UserRepository users, UserProfileRepository profiles, PasswordEncoder passwords, JwtService tokens,
            PasswordChangeRateLimiter passwordChanges) {
        this.users = users; this.profiles = profiles; this.passwords = passwords; this.tokens = tokens;
        this.passwordChanges = passwordChanges;
    }

    @Transactional
    public ProfileResponse register(RegisterRequest r) {
        String email = Identifiers.email(r.email()), phone = Identifiers.phone(r.phoneNumber());
        if (users.existsByEmail(email))
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "Email đã tồn tại.", Map.of("email", "Email đã tồn tại."));
        if (users.existsByPhoneNumber(phone))
            throw new ApiException(HttpStatus.CONFLICT, "PHONE_EXISTS", "Số điện thoại đã tồn tại.", Map.of("phoneNumber", "Số điện thoại đã tồn tại."));
        var account = new User();
        account.email = email; account.phoneNumber = phone; account.passwordHash = passwords.encode(r.password());
        users.saveAndFlush(account);
        var profile = new UserProfile();
        profile.user = account; profile.fullName = r.fullName().trim();
        profiles.saveAndFlush(profile);
        return ProfileResponse.from(account, profile);
    }

    @Transactional
    public LoginResponse login(LoginRequest r) {
        String identifier = r.identifier().trim();
        var account = (identifier.contains("@")
            ? users.findByEmail(Identifiers.email(identifier))
            : users.findByPhoneNumber(Identifiers.phone(identifier))).orElseThrow(this::badCredentials);
        if (account.status != User.Status.ACTIVE || account.deletedAt != null ||
            account.passwordHash == null ||
            r.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72 ||
            !passwords.matches(r.password(), account.passwordHash))
            throw badCredentials();
        var profile = profiles.findById(account.id).orElseThrow(() ->
            new ApiException(HttpStatus.NOT_FOUND, "PROFILE_NOT_FOUND", "Không tìm thấy hồ sơ của tài khoản."));
        account.lastLoginAt = Instant.now();
        users.saveAndFlush(account);
        return new LoginResponse(tokens.create(account.id), ProfileResponse.from(account, profile));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        passwordChanges.acquire(userId);
        // Serialize password changes so two requests cannot both verify an obsolete hash.
        var account = users.findLockedById(userId).orElseThrow(() ->
            new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Tài khoản không còn khả dụng."));
        if (account.status != User.Status.ACTIVE || account.deletedAt != null)
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Tài khoản không còn khả dụng.");
        if (account.passwordHash == null || !passwords.matches(request.currentPassword(), account.passwordHash))
            throw new ApiException(HttpStatus.BAD_REQUEST, "CURRENT_PASSWORD_INCORRECT", "Mật khẩu hiện tại không đúng.",
                Map.of("currentPassword", "Mật khẩu hiện tại không đúng."));
        if (request.currentPassword().equals(request.newPassword()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_UNCHANGED", "Mật khẩu mới phải khác mật khẩu hiện tại.",
                Map.of("newPassword", "Mật khẩu mới phải khác mật khẩu hiện tại."));
        account.passwordHash = passwords.encode(request.newPassword());
        users.saveAndFlush(account);
    }

    private ApiException badCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email/số điện thoại hoặc mật khẩu không đúng.");
    }
}
