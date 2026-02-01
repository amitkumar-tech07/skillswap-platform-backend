package com.backend.skillswap.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username or Email cannot be blank!")
    private String loginId;   // Username OR Email login (Hybrid login) supported


    @NotBlank(message = "Password cannot be blank!")
    private String password;

}
