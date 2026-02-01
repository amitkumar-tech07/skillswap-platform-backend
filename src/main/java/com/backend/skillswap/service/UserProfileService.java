package com.backend.skillswap.service;

import com.backend.skillswap.dto.request.UserProfileRequest;
import com.backend.skillswap.dto.response.UserProfileResponse;

public interface UserProfileService {

    // ================= USER =================
    UserProfileResponse getMyProfile();

    UserProfileResponse updateProfile(UserProfileRequest request);

    String uploadProfileImage(String imageUrl);

    // ================= PUBLIC =================
    UserProfileResponse getPublicProfile(Long userId);

    // ================= ADMIN =================
    UserProfileResponse getProfileByUserId(Long userId);

    void deactivateUser(Long userId);

    void activateUser(Long userId);

}
