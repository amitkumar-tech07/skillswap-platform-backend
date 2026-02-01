package com.backend.skillswap.service.impl;

import com.backend.skillswap.dto.request.UserProfileRequest;
import com.backend.skillswap.dto.response.UserProfileResponse;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.UserProfile;
import com.backend.skillswap.exception.authentication.InvalidCredentialsException;
import com.backend.skillswap.exception.common.ResourceNotFoundException;
import com.backend.skillswap.mapper.UserProfileMapper;
import com.backend.skillswap.repository.UserProfileRepository;
import com.backend.skillswap.repository.UserRepository;
import com.backend.skillswap.security.CustomUserDetails;
import com.backend.skillswap.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;

    // ================== GET MY PROFILE (USER) ==================
    @Override
    public UserProfileResponse getMyProfile() {
        Long userId = getLoggedInUserId();

        UserProfile profile = profileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        // Use Mapper to convert entity → DTO
        return UserProfileMapper.toResponse(profile);
    }

    // ================== UPDATE PROFILE ==================
    @Override
    public UserProfileResponse updateProfile(UserProfileRequest request) {

        Long userId = getLoggedInUserId();

        UserProfile profile = profileRepository.findByUser_Id(userId).orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        // Update fields from request DTO
        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getCountry() != null) profile.setCountry(request.getCountry());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());

        profileRepository.save(profile);

        return UserProfileMapper.toResponse(profile);
    }

    // ================== UPLOAD PROFILE IMAGE ==================
    @Override
    public String uploadProfileImage(String imageUrl) {

        Long userId = getLoggedInUserId();

        UserProfile profile = profileRepository.findByUser_Id(userId).orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        // uploadProfileImage validation
        if (imageUrl == null || imageUrl.isBlank() || !imageUrl.startsWith("http")) {
            throw new IllegalArgumentException("Invalid image URL");
        }

        profile.setProfileImage(imageUrl);
        profileRepository.save(profile);

        return "Profile image updated successfully";
    }

    // ================= PUBLIC ================= Public profile view (limited data) ==========

    @Transactional(readOnly = true)
    @Override
    public UserProfileResponse getPublicProfile(Long userId) {
        UserProfile profile = profileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        //  Do not expose sensitive fields (email, wallet, etc.)
        return UserProfileMapper.toPublicResponse(profile);
    }

    // ================= ADMIN (Admin can fetch any user's profile) =================

    @Override
    public UserProfileResponse getProfileByUserId(Long userId) {
        UserProfile profile = profileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return UserProfileMapper.toResponse(profile);
    }

    // Soft deactivate user account
    @Override
    public void deactivateUser(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // user already inactive
        if (!user.isActive()) {
            throw new IllegalStateException("User is already deactivated");
        }

        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void activateUser(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // user already active
        if (user.isActive()) {
            throw new IllegalStateException("User is already active");
        }

        user.setActive(true);
        userRepository.save(user);
    }

    // ================== SAFE FETCH LOGGED-IN USER ID ==================
    private Long getLoggedInUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated())
            throw new InvalidCredentialsException("User not authenticated");

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser().getId();
        }

        throw new InvalidCredentialsException("Invalid authentication principal");
    }

}
