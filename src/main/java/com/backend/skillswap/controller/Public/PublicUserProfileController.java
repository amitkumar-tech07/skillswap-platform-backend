package com.backend.skillswap.controller.Public;


import com.backend.skillswap.dto.response.UserProfileResponse;
import com.backend.skillswap.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/users")
@Tag(
        name = "Public User Profile APIs",
        description = "Public APIs to view user profile information"
)
@RequiredArgsConstructor
public class PublicUserProfileController {

    private final UserProfileService profileService;

    @Operation(summary = "Get public profile of a user")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getPublicProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getPublicProfile(userId));
    }
}
