package com.datn.fashion.dto.profile;

import com.datn.fashion.entity.User;
import com.datn.fashion.entity.UserProfile;

public record ProfileResponse(Long id, String fullName, String email, String phoneNumber, String avatarUrl) {
    public static ProfileResponse from(User account, UserProfile profile) {
        // Keep the existing Flutter String contract for accounts created before the phone migration.
        return new ProfileResponse(account.id, profile.fullName, account.email,
            account.phoneNumber == null ? "" : account.phoneNumber, profile.avatarUrl);
    }
}
