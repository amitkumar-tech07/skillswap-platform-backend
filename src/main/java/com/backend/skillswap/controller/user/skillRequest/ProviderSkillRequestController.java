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

import java.util.List;

@Tag(
        name = "Provider Skill Request APIs",
        description = """
Skill request APIs for PROVIDERS (skill owner).

PROVIDER can:
• View received requests
• Accept request
• Reject request
"""
)
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/provider/skill-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROVIDER')")
public class ProviderSkillRequestController {

    private final SkillRequestService skillRequestService;
    private final AuthService authService;

    private Long currentUserId() {
        return authService.getCurrentUserId();
    }

    // ================= VIEW RECEIVED REQUESTS =================
    @Operation(summary = "View received requests")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Received requests fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/received")
    public ResponseEntity<List<SkillRequestResponse>> myReceivedRequests() {
        return ResponseEntity.ok(
                skillRequestService.myReceivedRequests(currentUserId())
        );
    }

    // ================= ACCEPT REQUEST =================
    @Operation(summary = "Accept skill request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request accepted successfully"),
            @ApiResponse(responseCode = "400", description = "Only pending requests can be accepted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PutMapping("/{id}/accept")
    public ResponseEntity<SkillRequestResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(
                skillRequestService.acceptRequest(currentUserId(), id)
        );
    }

    // ================= REJECT REQUEST =================
    @Operation(summary = "Reject skill request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Only pending requests can be rejected"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<SkillRequestResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(
                skillRequestService.rejectRequest(currentUserId(), id)
        );
    }
}
