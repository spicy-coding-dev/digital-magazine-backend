package com.digital.magazine.common.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EmailService {

	void sendVerificationEmail(String toEmail, String token);

	public void sendVerificationEmailFromAdminAndSuper(String toEmail, String token);

	public void sendAdminVerificationEmail(String toEmail, String token);

	public void sendPasswordResetMail(String toEmail, String token);

	public void sendUserBlockedMail(String toEmail, String reason);

	public void sendUserUnblockedMail(String toEmail, String reason);

	public void sendSubscriptionBuyMail(String toEmail, String planName, String userName, LocalDate startDate,
			LocalDate endDate);

	public void sendSingleMagazineBuyMail(String toEmail, String magazineName, String userName, Long magazineNo,
			Double magazinePrice, LocalDateTime purchaseDate);

	void sendMail(String to, String subject, String content);

	public void sendMailWithAttachment(String to, String subject, String content, byte[] attachmentBytes,
			String fileName);

}
