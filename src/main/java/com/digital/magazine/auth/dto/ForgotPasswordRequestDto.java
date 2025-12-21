package com.digital.magazine.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
public class ForgotPasswordRequestDto {

    @Email
    @NotBlank
    private String email;
}
