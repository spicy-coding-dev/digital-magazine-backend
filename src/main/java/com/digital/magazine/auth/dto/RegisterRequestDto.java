package com.digital.magazine.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class RegisterRequestDto {

	@NotBlank
	private String name;

	@Email
	@NotBlank
	private String email;

	@NotBlank
	@Pattern(regexp = "^[6-9]\\d{9}$")
	private String mobile;

	@NotBlank
	@Size(min = 6)
	private String password;

	private String country;
	private String state;
	private String district;
}
