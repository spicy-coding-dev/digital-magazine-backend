package com.digital.magazine.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class UserSetPasswordDto {

	@NotBlank(message = "Token காலியாக இருக்கக்கூடாது")
	private String token;

	@NotBlank(message = "கடவுச்சொல் காலியாக இருக்கக்கூடாது")
	@Size(min = 6, message = "கடவுச்சொல் குறைந்தது 6 எழுத்துகள் இருக்க வேண்டும்")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{6,}$", message = "கடவுச்சொல் குறைந்தது 1 பெரிய எழுத்து, 1 சிறிய எழுத்து, 1 எண் மற்றும் 1 சிறப்பு எழுத்து கொண்டிருக்க வேண்டும்")
	private String password;

}
