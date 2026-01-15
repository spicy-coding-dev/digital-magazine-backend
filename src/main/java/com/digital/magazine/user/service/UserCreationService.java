package com.digital.magazine.user.service;

import org.springframework.security.core.Authentication;

import com.digital.magazine.common.enums.Role;
import com.digital.magazine.user.dto.CreateUserDto;
import com.digital.magazine.user.dto.UserSetPasswordDto;

public interface UserCreationService {

	public void createUser(CreateUserDto dto, Role targetRole, Authentication auth);

	public String verifyEmailAndSetPassword(UserSetPasswordDto dto);

}
