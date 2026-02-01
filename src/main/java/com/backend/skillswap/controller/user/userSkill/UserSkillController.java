package com.backend.skillswap.controller.user.userSkill;

import com.backend.skillswap.dto.common.ApiMessageResponse;
import com.backend.skillswap.dto.request.UserSkillRequest;
import com.backend.skillswap.dto.response.UserSkillResponse;
import com.backend.skillswap.service.AuthService;
import com.backend.skillswap.service.UserSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Tag(
        name = "User Skill APIs",
        description = """
🔐 Authenticated Skill Management APIs

Any authenticated USER & Provider can: 
• Add a new skill
• Update own skill
• Soft delete own skill
• Restore deleted skill
• View own skills

Note:
• PROVIDER is a USER with additional privileges
• Any Normal User can also add their skill but it cannot provide to another , He have to take Permission from Admin to change role from user to Provider.
"""
)
@RestController
@RequestMapping("/api/user/skills")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','PROVIDER')")
public class UserSkillController {

    private final UserSkillService userSkillService;
    private final AuthService authService;

    // Extract logged-in userId from JWT
    private Long currentUserId() {
        return authService.getCurrentUserId();
    }

    // ================= ADD SKILL =================
    @Operation(summary = "Add a new skill")
    @PostMapping
    public ResponseEntity<UserSkillResponse> addSkill(
            @Valid @RequestBody UserSkillRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userSkillService.addSkill(currentUserId(), request));
    }

    // ================= UPDATE SKILL =================
    @Operation(summary = "Update own skill")
    @PutMapping("/{skillId}")
    public ResponseEntity<UserSkillResponse> updateSkill(
            @PathVariable Long skillId,
            @Valid @RequestBody UserSkillRequest request
    ) {
        return ResponseEntity.ok(
                userSkillService.updateSkill(currentUserId(), skillId, request)
        );
    }

    // ================= DELETE SKILL (SOFT) =================
    @Operation(summary = "Delete own skill (Soft delete)")
    @DeleteMapping("/{skillId}")
    public ResponseEntity<ApiMessageResponse> deleteSkill(@PathVariable Long skillId) {
        userSkillService.deleteSkill(currentUserId(), skillId);
        return ResponseEntity.ok(new ApiMessageResponse("Skill deleted successfully"));
    }

    // ================= RESTORE SKILL =================
    @Operation(summary = "Restore deleted skill (Auto-unverify)")
    @PutMapping("/{skillId}/restore")
    public ResponseEntity<ApiMessageResponse> restoreSkill(
            @PathVariable Long skillId
    ) {
        userSkillService.restoreSkill(currentUserId(), skillId);
        return ResponseEntity.ok(
                new ApiMessageResponse("Skill restored successfully")
        );
    }

    // ================= GET MY SKILLS =================
    @Operation(summary = "Get my skills")
    @GetMapping("/me")
    public ResponseEntity<List<UserSkillResponse>> getMySkills() {
        return ResponseEntity.ok(
                userSkillService.getMySkills(currentUserId())
        );
    }

    // ================= INTERNAL / FUTURE USE =================
    @Operation(
            summary = "Get my skills using userId",
            description = """
Returns active skills using userId directly.
Useful for internal calls or future microservices.
"""
    )
    @GetMapping("/me/by-user-id")
    public ResponseEntity<List<UserSkillResponse>> getMySkillsByUserId() {
        return ResponseEntity.ok(userSkillService.getMySkillsByUserId(currentUserId()));
    }
}
