package com.digital.magazine.common.service;

public interface EmailService {

	void sendVerificationEmail(String toEmail, String token);

	public void sendVerificationEmailFromAdminAndSuper(String toEmail, String token);

	public void sendAdminVerificationEmail(String toEmail, String token);

	public void sendPasswordResetMail(String toEmail, String token);

	public void sendUserBlockedMail(String toEmail, String reason);

	public void sendUserUnblockedMail(String toEmail, String reason);

	void sendEmail(String to, String subject, String content);

	public void sendMailWithAttachment(String to, String subject, String content, byte[] attachmentBytes,
			String fileName);

}
