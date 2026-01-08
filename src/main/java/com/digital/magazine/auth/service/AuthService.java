package com.digital.magazine.auth.service;

import com.digital.magazine.auth.dto.ForgotPasswordRequestDto;
import com.digital.magazine.auth.dto.LoginRequestDto;
import com.digital.magazine.auth.dto.RegisterRequestDto;
import com.digital.magazine.auth.dto.ResetPasswordRequestDto;

import com.digital.magazine.common.response.LoginApiResponse;
import com.digital.magazine.user.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	void register(RegisterRequestDto dto);

	String verifyEmail(String token);

	public LoginApiResponse login(LoginRequestDto request, HttpServletResponse resp);

	public LoginApiResponse refreshToken(HttpServletRequest request);

	public void generateResetToken(ForgotPasswordRequestDto dto);

	public void resetPassword(ResetPasswordRequestDto dto);

	public void logout(HttpServletResponse response);
	
	User findByEmail(String email);

}
