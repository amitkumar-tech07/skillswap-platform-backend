package com.backend.skillswap.service;

import com.backend.skillswap.dto.request.UserSkillRequest;
import com.backend.skillswap.dto.response.UserSkillResponse;
import com.backend.skillswap.entity.enums.SkillCategory;

import java.util.List;

public interface UserSkillService {

    // ================= USER ACTIONS =================

    // User adds a new skill
    UserSkillResponse addSkill(Long userId, UserSkillRequest request);

    // User updates own skill
    UserSkillResponse updateSkill(Long userId, Long skillId, UserSkillRequest request);

    // User deletes (soft delete) own skill
    void deleteSkill(Long userId, Long skillId);

    // Logged-in user → apni sari skills
    List<UserSkillResponse> getMySkills(Long userId);


    // Get logged-in user's skills using userId only
    List<UserSkillResponse> getMySkillsByUserId(Long userId);

    // RESTORE SKILL
    void restoreSkill(Long userId, Long skillId);

    // ================= ADMIN ACTIONS =================

    // Admin verifies skill (publicly visible)
    void verifySkill(Long skillId);

    // Admin rejects / disables skill
    void rejectSkill(Long skillId);

    // Get ALL skills (active + inactive) of a user
    List<UserSkillResponse> getAllSkillsOfUser(Long userId);

    // Get all unverified skills (admin moderation queue)
    List<UserSkillResponse> getUnverifiedSkills();

    // ================= PUBLIC OPERATIONS =================

    // Get verified & active skills by category
    List<UserSkillResponse> getSkillsByCategory(SkillCategory category);

    // Public: all verified skills
    List<UserSkillResponse> getAllVerifiedSkills();

    // Public profile → kisi user ki verified skills
    List<UserSkillResponse> getUserSkills(Long userId);

    // Public search (verified + active only)
    List<UserSkillResponse> searchSkills(String keyword);

}
