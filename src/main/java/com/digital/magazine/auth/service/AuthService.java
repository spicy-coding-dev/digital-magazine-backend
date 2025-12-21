package com.digital.magazine.auth.service;

import com.digital.magazine.auth.dto.RegisterRequestDto;

public interface AuthService {

	void register(RegisterRequestDto dto);

	String verifyEmail(String token);

}
