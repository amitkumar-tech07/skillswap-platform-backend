package com.backend.skillswap.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;

}
