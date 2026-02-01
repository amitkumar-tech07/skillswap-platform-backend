package com.backend.skillswap.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyResetOtpRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid format!")
    private String email;

    @NotBlank
    private String otp;
}
