package com.datn.fashion.dto.profile;
import jakarta.validation.constraints.*;
public record UpdateProfileRequest(
 @NotBlank(message="Vui lòng nhập họ và tên.") @Size(max=150) String fullName,
 @NotBlank(message="Vui lòng nhập số điện thoại.") @Size(max=20) @Pattern(regexp="^(0[35789][0-9]{8}|\\+84[35789][0-9]{8})$",message="Số điện thoại Việt Nam không hợp lệ.") String phoneNumber
) {}
