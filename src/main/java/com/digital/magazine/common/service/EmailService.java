package com.digital.magazine.common.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.digital.magazine.subscription.entity.UserAddress;
import com.digital.magazine.subscription.enums.SubscriptionType;

public interface EmailService {

	void sendVerificationEmail(String toEmail, String token);

	public void sendVerificationEmailFromAdminAndSuper(String toEmail, String token);

	public void sendAdminVerificationEmail(String toEmail, String token);

	public void sendPasswordResetMail(String toEmail, String token);

	public void sendUserBlockedMail(String toEmail, String reason);

	public void sendUserUnblockedMail(String toEmail, String reason);

	public void sendDigitalSubscriptionBuyMail(String toEmail, SubscriptionType planType, String userName,
			LocalDate startDate, LocalDate endDate);

	public void sendPrintSubscriptionBuyMail(String toEmail, SubscriptionType planType, String userName,
			UserAddress Address, LocalDate startDate, LocalDate endDate);

	public void sendSingleMagazineBuyMail(String toEmail, String magazineName, String userName, Long magazineNo,
			Double magazinePrice, LocalDateTime purchaseDate);

	void sendMail(String to, String subject, String content);

	public void sendMailWithAttachment(String to, String subject, String content, byte[] attachmentBytes,
			String fileName);

}
