package com.backend.skillswap.controller.admin;

import com.backend.skillswap.config.OpenApiConfig;
import com.backend.skillswap.dto.common.ApiMessageResponse;
import com.backend.skillswap.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(
        name = "Admin User Role APIs",
        description = """
🔐 Admin-only user role management

✔ Grant PROVIDER role
✔ Remove PROVIDER role
"""
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserRoleController {

    private final AuthService authService;

    // ======================= GRANT PROVIDER ROLE ======================= //

    @Operation(
            summary = "Grant PROVIDER role to user",
            description = "Promotes a USER to PROVIDER role"
    )
    @PostMapping("/{userId}/grant-provider")
    public ResponseEntity<ApiMessageResponse> grantProvider(@PathVariable Long userId) {
        authService.approveProviderRole(userId);
        return ResponseEntity.ok(
                new ApiMessageResponse("✅ PROVIDER role granted successfully")
        );
    }

    // ======================= REMOVE PROVIDER ROLE ======================= //

    @Operation(
            summary = "Remove PROVIDER role from user",
            description = "Removes PROVIDER role, USER role remains"
    )
    @ApiResponse(responseCode = "200", description = "Provider role removed successfully")
    @PutMapping("/{userId}/remove-provider")
    public ResponseEntity<ApiMessageResponse> removeProviderRole(@PathVariable Long userId) {
        authService.removeProviderRole(userId);
        return ResponseEntity.ok(
                new ApiMessageResponse("❌ PROVIDER role removed successfully")
        );
    }
}
