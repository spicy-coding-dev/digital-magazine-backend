package com.digital.magazine.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
public class ForgotPasswordRequestDto {

	@NotBlank(message = "மின்னஞ்சல் அல்லது மொபைல் எண் உள்ளிடவும்")
    private String emailOrMobile;
}
