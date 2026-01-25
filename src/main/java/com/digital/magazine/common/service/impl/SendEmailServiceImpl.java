package com.digital.magazine.common.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.digital.magazine.common.enums.MailTargetType;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.common.service.EmailService;
import com.digital.magazine.common.service.SendEmailService;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailServiceImpl implements SendEmailService {

	private final UserRepository userRepo;
	private final MagazinePurchaseRepository magazinePurchaseRepo;
	private final UserSubscriptionRepository userSubscriptionRepo;
	private EmailService mailSender;

	public void sendBulkMail(MailTargetType targetType, AccountStatus accountStatus, String subject, String content,
			byte[] attachment, String fileName) {

		log.info("📨 Preparing bulk mail | targetType={}", targetType);

		List<String> emails;

		switch (targetType) {

		case ACCOUNT_STATUS -> {
			emails = userRepo.findEmailsByStatusAndRole(accountStatus, Role.USER);
		}

		case DIGITAL_SUBSCRIPTION -> {
			emails = userSubscriptionRepo.findEmailsBySubscriptionType(SubscriptionType.DIGITAL);
		}

		case PRINT_SUBSCRIPTION -> {
			emails = userSubscriptionRepo.findEmailsBySubscriptionType(SubscriptionType.PRINT);
		}

		case SINGLE_PURCHASE -> {
			emails = magazinePurchaseRepo.findDistinctBuyerEmails();
		}

		default -> throw new IllegalStateException("Invalid mail target");
		}

		if (emails.isEmpty()) {
			log.warn("⚠️ No recipients found | targetType={}", targetType);
			return;
		}

		sendMails(emails, subject, content, attachment, fileName);
	}

	private void sendMails(List<String> emails, String subject, String content, byte[] attachment, String fileName) {

		for (String email : emails) {
			try {
				if (attachment != null) {
					log.debug("📎 Sending mail WITH attachment to {}", email);
					mailSender.sendMailWithAttachment(email, subject, content, attachment, fileName);
				} else {
					log.debug("📨 Sending normal mail to {}", email);
					mailSender.sendEmail(email, subject, content);
				}
				log.info("✅ Mail sent | email={}", email);
			} catch (Exception e) {
				log.error("❌ Failed to send mail | email={}, reason={}", email, e.getMessage(), e);
			}
		}
	}

}
