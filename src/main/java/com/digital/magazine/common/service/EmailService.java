package com.digital.magazine.common.service;

public interface EmailService {

	void sendVerificationEmail(String toEmail, String token);

	void sendForgotPasswordEmail(String toEmail, String token);

}
