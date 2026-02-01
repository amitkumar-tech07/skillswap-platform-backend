package com.backend.skillswap.mapper;

import com.backend.skillswap.dto.request.UserSkillRequest;
import com.backend.skillswap.dto.response.UserSkillResponse;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.UserSkill;

public class UserSkillMapper {

    // Entity → Response DTO
    public static UserSkillResponse toResponse(UserSkill skill) {
        return UserSkillResponse.builder()
                .id(skill.getId())
                .userId(skill.getUser().getId())
                .title(skill.getTitle())
                .description(skill.getDescription())
                .category(skill.getCategory())
                .level(skill.getLevel())
                .experienceYears(skill.getExperienceYears())
                .hourlyRate(skill.getHourlyRate())
                .active(skill.isActive())
                .verified(skill.isVerified())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }

    // Request DTO → Entity (Create)
    public static UserSkill toEntity(UserSkillRequest dto, UserEntity user) {
        return UserSkill.builder()
                .user(user)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .level(dto.getLevel())
                .experienceYears(dto.getExperienceYears())
                .hourlyRate(dto.getHourlyRate())
                .active(true)       //  default active on create
                .verified(false)    //  default unverified
                .build();
    }

    // Update existing UserSkill
    public static void updateEntity(UserSkill skill, UserSkillRequest dto) {
        skill.setTitle(dto.getTitle());
        skill.setDescription(dto.getDescription());
        skill.setCategory(dto.getCategory());
        skill.setLevel(dto.getLevel());
        skill.setExperienceYears(dto.getExperienceYears());
        skill.setHourlyRate(dto.getHourlyRate());

        // Auto-unverify on update
        skill.setVerified(false);
    }
}
