package com.backend.skillswap.mapper;

import com.backend.skillswap.dto.response.UserProfileResponse;
import com.backend.skillswap.entity.UserProfile;

public class UserProfileMapper {

    private UserProfileMapper() {
        // prevent instantiation
    }

    // FULL PROFILE RESPONSE It is used for: Logged-in user & Admin
    public static UserProfileResponse toResponse(UserProfile profile) {
        if (profile == null) return null;

        return UserProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .bio(profile.getBio())
                .country(profile.getCountry())
                .location(profile.getLocation())
                .profileImage(profile.getProfileImage())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    // PUBLIC PROFILE RESPONSE
    // Used for: Public profile view  & Hide Sensitive / internal fields intentionally hidden
    public static UserProfileResponse toPublicResponse(UserProfile profile) {
        if (profile == null) return null;

        return UserProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .bio(profile.getBio())
                .country(profile.getCountry())
                .location(profile.getLocation())
                .profileImage(profile.getProfileImage())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
