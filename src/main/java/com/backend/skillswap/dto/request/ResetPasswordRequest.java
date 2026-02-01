package com.backend.skillswap.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    @Email(message = "Email must be valid format!")
    @NotBlank(message = "Email cannot be blank!")
    private String email;

    @NotBlank(message = "New password cannot be blank!")
    @Size(min = 8, max = 50, message = "Password must be at least 8 characters long")
    private String newPassword;

    @NotBlank(message = "Confirm New password cannot be blank!")
    private String confirmNewPassword;


}
