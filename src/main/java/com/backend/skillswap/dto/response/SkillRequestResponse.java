package com.backend.skillswap.dto.response;

import com.backend.skillswap.entity.enums.SkillRequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkillRequestResponse {

    private Long id;

    // Sender info
    private Long senderId;
    private String senderUsername;

    // Receiver info
    private Long receiverId;
    private String receiverUsername;

    // Skill info
    private Long skillId;
    private String skillTitle;

    // Request details
    private String message;
    private SkillRequestStatus status;

    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
