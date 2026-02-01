package com.backend.skillswap.mapper;

import com.backend.skillswap.dto.request.SkillRequestRequest;
import com.backend.skillswap.dto.response.SkillRequestResponse;
import com.backend.skillswap.entity.SkillRequest;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.UserSkill;

public class SkillRequestMapper {

    private SkillRequestMapper() {
        // Utility class
    }

    // ENTITY → RESPONSE
    public static SkillRequestResponse toResponse(SkillRequest request) {

        return SkillRequestResponse.builder()
                .id(request.getId())

                // Sender
                .senderId(request.getSender().getId())
                .senderUsername(request.getSender().getUsername())

                // Receiver
                .receiverId(request.getReceiver().getId())
                .receiverUsername(request.getReceiver().getUsername())

                // Skill
                .skillId(request.getSkill().getId())
                .skillTitle(request.getSkill().getTitle())

                // Request data
                .message(request.getMessage())
                .status(request.getStatus())

                .expiresAt(request.getExpiresAt())
                .createdAt(request.getCreatedAt())
                .build();
    }

    // REQUEST → ENTITY (CREATE)
    public static SkillRequest toEntity(
            SkillRequestRequest dto,
            UserEntity sender,
            UserEntity receiver,
            UserSkill skill
    ) {
        return SkillRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .skill(skill)
                .message(dto.getMessage())
                .build(); // status & timestamps handled internally
    }
}
