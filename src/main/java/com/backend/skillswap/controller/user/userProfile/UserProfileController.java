package com.backend.skillswap.controller.user.userProfile;

import com.backend.skillswap.dto.common.ApiMessageResponse;
import com.backend.skillswap.dto.request.UserProfileRequest;
import com.backend.skillswap.dto.response.UserProfileResponse;
import com.backend.skillswap.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(
        name = "User Profile APIs",
        description = """
🔐 Authenticated User Profile APIs

An authenticated user can:
• View their own profile
• Update personal profile details
• Upload or update profile image

Note:  Users cannot view or modify other users' profiles
"""
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user/profile") // Base path for profile APIs
public class UserProfileController {

    private final UserProfileService profileService; // Inject service layer

    // =================  Get My Profile ================= //
    @Operation(
            summary = "Get my profile",
            description = "Fetches profile information of the currently logged-in user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))), // Successful fetch
            @ApiResponse(responseCode = "404", description = "Profile not found") // Profile does not exist
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        // Returns current user's profile from service
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    // ================= Update Profile ================= //
    @Operation(
            summary = "Update my profile",
            description = "Updates profile details of the currently logged-in user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))), // Successful update
            @ApiResponse(responseCode = "400", description = "Invalid profile data") // Invalid input
    })
    @PutMapping("/update")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UserProfileRequest request) {
        // Delegates update to service
        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    // ================= Upload Profile Image ================= //
    @Operation(
            summary = "Upload profile image",
            description = "Uploads or updates the profile image of the logged-in user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile image updated successfully"), // Successful image upload
            @ApiResponse(responseCode = "400", description = "Invalid image URL") // Invalid image URL
    })
    @PutMapping("/upload-image")
    public ResponseEntity<ApiMessageResponse> uploadImage(@RequestParam String imageUrl) {

        return ResponseEntity.ok(new ApiMessageResponse(profileService.uploadProfileImage(imageUrl)));
    }
}
