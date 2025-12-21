package com.digital.magazine.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class UserResponseDto {

	private Long id;
	private String name;
	private String email;
	private String mobile;
	private String role;
	private boolean emailVerified;
}
