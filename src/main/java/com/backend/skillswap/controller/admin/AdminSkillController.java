package com.backend.skillswap.controller.admin;

import com.backend.skillswap.config.OpenApiConfig;
import com.backend.skillswap.dto.common.ApiMessageResponse;
import com.backend.skillswap.dto.response.UserSkillResponse;
import com.backend.skillswap.service.UserSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/skills")
@RequiredArgsConstructor
@Tag(
        name = "15. Admin Skill Moderation APIs",
        description = """
🛠 Admin skill moderation

✔ Verify skills
✔ Reject / disable skills
✔ View unverified skills
✔ View all skills of a user
"""
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("hasRole('ADMIN')")
public class AdminSkillController {

    private final UserSkillService userSkillService;

    // ======================= VERIFY SKILL ======================= //

    @Operation(
            summary = "Verify a user skill",
            description = "Marks skill as VERIFIED and makes it public"
    )
    @PatchMapping("/{skillId}/verify")
    public ResponseEntity<ApiMessageResponse> verifySkill(@PathVariable Long skillId) {
        userSkillService.verifySkill(skillId);
        return ResponseEntity.ok(
                new ApiMessageResponse("✅ Skill verified successfully")
        );
    }

    // ======================= REJECT / DISABLE SKILL ======================= //

    @Operation(
            summary = "Reject / disable a user skill",
            description = "Marks skill inactive and removes public visibility"
    )
    @PatchMapping("/{skillId}/reject")
    public ResponseEntity<ApiMessageResponse> rejectSkill(@PathVariable Long skillId) {
        userSkillService.rejectSkill(skillId);
        return ResponseEntity.ok(
                new ApiMessageResponse("❌ Skill rejected successfully")
        );
    }

    // ======================= GET UNVERIFIED SKILLS ======================= //

    @Operation(summary = "Get all unverified skills")
    @GetMapping("/unverified")
    public ResponseEntity<List<UserSkillResponse>> getUnverifiedSkills() {
        return ResponseEntity.ok(
                userSkillService.getUnverifiedSkills()
        );
    }

    // ======================= GET ALL SKILLS OF USER ======================= //

    @Operation(summary = "Get all skills of a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserSkillResponse>> getAllSkillsOfUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                userSkillService.getAllSkillsOfUser(userId)
        );
    }
}
