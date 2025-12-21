package com.digital.magazine.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
public class ResetPasswordRequestDto {

    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
