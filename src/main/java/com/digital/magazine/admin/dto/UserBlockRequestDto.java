package com.digital.magazine.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBlockRequestDto {

    @NotBlank(message = "Block reason is mandatory")
    private String reason;
}
