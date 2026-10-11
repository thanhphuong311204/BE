package com.datn.fashion.dto.profile;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** Missing optional properties preserve existing values; explicit null clears them. */
public class UpdateProfileRequest {
    @NotBlank(message = "Vui lòng nhập họ và tên.") @Size(max = 150)
    public String fullName;
    @NotBlank(message = "Vui lòng nhập số điện thoại.") @Size(max = 20)
    @Pattern(regexp = "^(0[35789][0-9]{8}|\\+84[35789][0-9]{8})$",
        message = "Số điện thoại Việt Nam không hợp lệ.")
    public String phoneNumber;
    @PastOrPresent(message = "Ngày sinh không được sau hôm nay.")
    private LocalDate dateOfBirth;
    @Pattern(regexp = "FEMALE|MALE|OTHER|PREFER_NOT_TO_SAY", message = "Giới tính không hợp lệ.")
    private String gender;
    private boolean dateOfBirthProvided;
    private boolean genderProvided;

    public String fullName() { return fullName; }
    public String phoneNumber() { return phoneNumber; }
    public LocalDate dateOfBirth() { return dateOfBirth; }
    public String gender() { return gender; }
    public boolean dateOfBirthProvided() { return dateOfBirthProvided; }
    public boolean genderProvided() { return genderProvided; }
    @JsonSetter("dateOfBirth")
    public void setDateOfBirth(LocalDate value) {
        dateOfBirth = value; dateOfBirthProvided = true;
    }
    @JsonSetter("gender")
    public void setGender(String value) {
        gender = value; genderProvided = true;
    }
}
