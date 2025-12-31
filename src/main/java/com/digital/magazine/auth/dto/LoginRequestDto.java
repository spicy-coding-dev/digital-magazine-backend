package com.digital.magazine.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class LoginRequestDto {

	@NotBlank(message = "பயனர் மின்னஞ்சல் அல்லது மொபைல் எண்ணை உள்ளிடவும்")
	private String emailOrPhone;
	@NotBlank(message = "கடவுச்சொல் காலியாக இருக்கக்கூடாது")
	@Size(min = 6, message = "கடவுச்சொல் குறைந்தது 6 எழுத்துகள் இருக்க வேண்டும்")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,}$", message = "கடவுச்சொல் குறைந்தது 1 பெரிய எழுத்து, 1 சிறிய எழுத்து, 1 எண் மற்றும் 1–3 சிறப்பு எழுத்துக்கள் கொண்டிருக்க வேண்டும்")
	private String password;

	private String captchaResponse;
}
