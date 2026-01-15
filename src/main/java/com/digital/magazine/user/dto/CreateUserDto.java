package com.digital.magazine.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class CreateUserDto {

	@NotBlank(message = "பெயர் காலியாக இருக்கக்கூடாது")
	private String name;

	@NotBlank(message = "மின்னஞ்சல் காலியாக இருக்கக்கூடாது")
	@Email(message = "சரியான மின்னஞ்சல் முகவரியை உள்ளிடவும்")
	private String email;

	@NotBlank(message = "மொபைல் எண் காலியாக இருக்கக்கூடாது")
	@Pattern(regexp = "^[0-9]{10}$", message = "மொபைல் எண் 10 இலக்கமாக இருக்க வேண்டும்")
	private String mobile;

	private String country;
	private String state;
	private String district;
}
