package com.datn.fashion.dto.auth;
import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;
public record RegisterRequest(
 @NotBlank(message="Vui lòng nhập họ và tên.") @Size(max=150) String fullName,
 @NotBlank(message="Vui lòng nhập email.") @Email(message="Email không đúng định dạng.") @Size(max=254) String email,
 @NotBlank(message="Vui lòng nhập số điện thoại.") @Size(max=20) @Pattern(regexp="^(0[35789][0-9]{8}|\\+84[35789][0-9]{8})$",message="Số điện thoại Việt Nam không hợp lệ.") String phoneNumber,
 @NotBlank(message="Vui lòng nhập mật khẩu.") @Size(min=6,max=72,message="Mật khẩu cần từ 6 đến 72 ký tự.") String password,
 @NotBlank(message="Vui lòng xác nhận mật khẩu.") String confirmPassword
) {
 @Override public String toString() { return "RegisterRequest[credentials=REDACTED]"; }
 @AssertTrue(message="Mật khẩu xác nhận không trùng khớp.")
 public boolean isPasswordMatching() { return password!=null && password.equals(confirmPassword); }
 @AssertTrue(message="Mật khẩu không được vượt quá 72 byte UTF-8.")
 public boolean isPasswordWithinByteLimit() { return password==null || password.getBytes(StandardCharsets.UTF_8).length<=72; }
}
