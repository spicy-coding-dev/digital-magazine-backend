package com.digital.magazine.common.service;

public interface EmailService {

	void sendVerificationEmail(String toEmail, String token);

	public void sendAdminVerificationEmail(String toEmail, String token);

	public void sendPasswordResetMail(String toEmail, String token);

}
