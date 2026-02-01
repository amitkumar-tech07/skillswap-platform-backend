package com.backend.skillswap.controller.Public;

import com.backend.skillswap.dto.response.UserSkillResponse;
import com.backend.skillswap.entity.enums.SkillCategory;
import com.backend.skillswap.service.UserSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(
        name = "Public Skill APIs",
        description = """
🌍 Public Skill Browsing APIs

Anyone can:
• View verified skills
• Search skills
• Filter by category
• View public profile skills
"""
)
@RestController
@RequestMapping("/api/public/skills")
@RequiredArgsConstructor
public class PublicSkillController {

    private final UserSkillService userSkillService;

    // ================= GET ALL VERIFIED SKILLS =================
    @Operation(summary = "Get all verified skills")
    @GetMapping("/verified")
    public ResponseEntity<List<UserSkillResponse>> getAllVerifiedSkills() {
        return ResponseEntity.ok(
                userSkillService.getAllVerifiedSkills()
        );
    }

    // ================= GET USER PUBLIC SKILLS =================
    @Operation(summary = "Get verified skills of a user (Public profile)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserSkillResponse>> getUserSkills(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                userSkillService.getUserSkills(userId)
        );
    }

    // ================= SEARCH SKILLS =================
    @Operation(summary = "Search verified skills by keyword")
    @GetMapping("/search")
    public ResponseEntity<List<UserSkillResponse>> searchSkills(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(
                userSkillService.searchSkills(keyword)
        );
    }

    // ================= FILTER BY CATEGORY =================
    @Operation(summary = "Get verified skills by category")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<UserSkillResponse>> getSkillsByCategory(
            @PathVariable SkillCategory category
    ) {
        return ResponseEntity.ok(
                userSkillService.getSkillsByCategory(category)
        );
    }
}
