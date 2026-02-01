package com.backend.skillswap.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String bio;
    private String country;
    private String profileImage;
    private String location;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}


