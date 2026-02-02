package com.backend.skillswap.controller.admin;


import com.backend.skillswap.dto.common.ApiMessageResponse;
import com.backend.skillswap.dto.response.UserProfileResponse;
import com.backend.skillswap.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "17. Admin User Profile APIs", description = "Admin APIs to manage user profiles")
@RequiredArgsConstructor
public class AdminUserProfileController {

    private final UserProfileService profileService;

    @Operation(summary = "Get any user's profile (Admin)")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    @Operation(summary = "Deactivate a user profile")
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<ApiMessageResponse> deactivateUser(@PathVariable Long userId) {
        profileService.deactivateUser(userId);
        return ResponseEntity.ok(new ApiMessageResponse("User deactivated successfully"));
    }

    @Operation(summary = "Activate a user profile")
    @PutMapping("/{userId}/activate")
    public ResponseEntity<ApiMessageResponse> activateUser(@PathVariable Long userId) {

        profileService.activateUser(userId);

        return ResponseEntity.ok(
                new ApiMessageResponse("User activated successfully")
        );
    }

}
