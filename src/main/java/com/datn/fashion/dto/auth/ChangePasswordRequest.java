package com.datn.fashion.dto.auth;

import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;

public record ChangePasswordRequest(
    @NotBlank(message="Vui lòng nhập mật khẩu hiện tại.")
    @Size(max=72, message="Mật khẩu không được vượt quá 72 ký tự.") String currentPassword,
    @NotBlank(message="Vui lòng nhập mật khẩu mới.")
    @Size(min=6,max=72,message="Mật khẩu mới cần từ 6 đến 72 ký tự.") String newPassword,
    @NotBlank(message="Vui lòng xác nhận mật khẩu mới.")
    @Size(max=72) String confirmPassword
) {
    @Override public String toString() { return "ChangePasswordRequest[credentials=REDACTED]"; }
    @AssertTrue(message="Mật khẩu xác nhận không trùng khớp.")
    public boolean isPasswordMatching() { return newPassword != null && newPassword.equals(confirmPassword); }
    @AssertTrue(message="Mật khẩu mới không được vượt quá 72 byte UTF-8.")
    public boolean isNewPasswordWithinByteLimit() { return withinLimit(newPassword); }
    @AssertTrue(message="Mật khẩu hiện tại không được vượt quá 72 byte UTF-8.")
    public boolean isCurrentPasswordWithinByteLimit() { return withinLimit(currentPassword); }
    private static boolean withinLimit(String value) {
        return value == null || value.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
