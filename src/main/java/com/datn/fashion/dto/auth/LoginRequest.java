package com.datn.fashion.dto.auth;
import jakarta.validation.constraints.*;
public record LoginRequest(
 @NotBlank(message="Vui lòng nhập email hoặc số điện thoại.") @Size(max=254) String identifier,
 @NotBlank(message="Vui lòng nhập mật khẩu.") @Size(max=72) String password
) { @Override public String toString() { return "LoginRequest[credentials=REDACTED]"; } }
