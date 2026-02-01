package com.backend.skillswap.controller.user.skillRequest;


import com.backend.skillswap.dto.request.SkillRequestRequest;
import com.backend.skillswap.dto.response.SkillRequestResponse;
import com.backend.skillswap.service.AuthService;
import com.backend.skillswap.service.SkillRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "User Skill Request APIs",
        description = """
Skill request APIs for USERS (request sender).

USER can:
• Send skill request
• View sent requests
• Cancel own pending request
"""
)
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/user/skill-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserSkillRequestController {

    private final SkillRequestService skillRequestService;
    private final AuthService authService;

    private Long currentUserId() {
        return authService.getCurrentUserId();
    }

    // ================= SEND REQUEST =================
    @Operation(summary = "Send a skill request")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Skill request sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or duplicate request"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Skill not found")
    })
    @PostMapping
    public ResponseEntity<SkillRequestResponse> sendRequest(
            @Valid @RequestBody SkillRequestRequest request
    ) {
        return ResponseEntity.status(201)
                .body(skillRequestService.sendRequest(currentUserId(), request));
    }

    // ================= VIEW SENT REQUESTS =================
    @Operation(summary = "View my sent requests")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sent requests fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/sent")
    public ResponseEntity<List<SkillRequestResponse>> mySentRequests() {
        return ResponseEntity.ok(
                skillRequestService.mySentRequests(currentUserId())
        );
    }

    // ================= CANCEL REQUEST =================
    @Operation(summary = "Cancel sent request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Only pending requests can be cancelled"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SkillRequestResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
                skillRequestService.cancelRequest(currentUserId(), id)
        );
    }
}
