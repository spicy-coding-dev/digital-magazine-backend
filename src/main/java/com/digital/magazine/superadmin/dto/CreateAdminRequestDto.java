package com.digital.magazine.superadmin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAdminRequestDto {
	
	@NotBlank(message = "பயனர் பெயர் காலியாக இருக்கக்கூடாது")
	private String name;

	@NotBlank(message = "மின்னஞ்சல் காலியாக இருக்கக்கூடாது")
	@Email(message = "சரியான மின்னஞ்சல் முகவரியை உள்ளிடவும்")
	private String email;

	@NotBlank(message = "மொபைல் எண் காலியாக இருக்கக்கூடாது")
	@Pattern(regexp = "^[0-9]{10}$", message = "மொபைல் எண் 10 இலக்கமாக இருக்க வேண்டும்")
	private String mobile;

	@NotBlank(message = "கடவுச்சொல் காலியாக இருக்கக்கூடாது")
	@Size(min = 6, message = "கடவுச்சொல் குறைந்தது 6 எழுத்துகள் இருக்க வேண்டும்")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,}$", message = "கடவுச்சொல் குறைந்தது 1 பெரிய எழுத்து, 1 சிறிய எழுத்து, 1 எண் மற்றும் 1–3 சிறப்பு எழுத்துக்கள் கொண்டிருக்க வேண்டும்")
	private String password;

	private String country;
	private String state;
	private String district;

}
