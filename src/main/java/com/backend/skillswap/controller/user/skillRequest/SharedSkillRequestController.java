package com.backend.skillswap.controller.user.skillRequest;

import com.backend.skillswap.dto.response.SkillRequestResponse;
import com.backend.skillswap.service.AuthService;
import com.backend.skillswap.service.SkillRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Tag(
        name = "09. Shared Skill Request APIs",
        description = """
Shared skill request actions.

Accessible by:
• USER (sender)
• PROVIDER (receiver)
"""
)
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/skill-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','PROVIDER')")
public class SharedSkillRequestController {

    private final SkillRequestService skillRequestService;
    private final AuthService authService;

    private Long currentUserId() {
        return authService.getCurrentUserId();
    }

    // ================= MARK COMPLETED =================
    @Operation(summary = "Mark request as completed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request marked as completed"),
            @ApiResponse(responseCode = "400", description = "Only accepted requests can be completed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PutMapping("/{id}/complete")
    public ResponseEntity<SkillRequestResponse> markCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(
                skillRequestService.markCompleted(currentUserId(), id)
        );
    }
}
