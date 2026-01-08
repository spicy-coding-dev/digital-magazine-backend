package com.digital.magazine.auth.dto;

import com.digital.magazine.common.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileDto {
	
	private String name;
	private String email;
	private Role role;

}
