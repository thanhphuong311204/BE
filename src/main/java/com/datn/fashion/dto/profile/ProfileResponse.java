package com.datn.fashion.dto.profile;

import com.datn.fashion.entity.User;
import com.datn.fashion.entity.UserProfile;
import java.time.LocalDate;

public record ProfileResponse(Long id, String fullName, String email, String phoneNumber,
    String avatarUrl, LocalDate dateOfBirth, UserProfile.Gender gender) {
    public static ProfileResponse from(User account, UserProfile profile) {
        return new ProfileResponse(account.id, profile.fullName, account.email,
            account.phoneNumber == null ? "" : account.phoneNumber, profile.avatarUrl,
            profile.dateOfBirth, profile.gender);
    }
}
