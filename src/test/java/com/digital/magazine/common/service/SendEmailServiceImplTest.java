package com.digital.magazine.common.service;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.digital.magazine.common.enums.MailTargetType;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.common.service.impl.SendEmailServiceImpl;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SendEmailServiceImplTest {

	@Mock
	private UserRepository userRepo;

	@Mock
	private MagazinePurchaseRepository magazinePurchaseRepo;

	@Mock
	private UserSubscriptionRepository userSubscriptionRepo;

	@Mock
	private EmailService emailService;

	@InjectMocks
	private SendEmailServiceImpl sendEmailService;

	private final String SUBJECT = "Test Subject";
	private final String CONTENT = "Test Content";

	@BeforeEach
	void setup() {
		// nothing special
	}

	// ✅ ACCOUNT_STATUS → normal email
	@Test
	void sendBulkMail_accountStatus_success() {

		when(userRepo.findEmailByStatusAndRole(AccountStatus.ACTIVE, Role.USER))
				.thenReturn(List.of("a@test.com", "b@test.com"));

		sendEmailService.sendBulkMail(MailTargetType.ACCOUNT_STATUS, AccountStatus.ACTIVE, SUBJECT, CONTENT, null,
				null);

		verify(emailService, times(2)).sendEmail(anyString(), eq(SUBJECT), eq(CONTENT));

		verify(emailService, never()).sendMailWithAttachment(any(), any(), any(), any(), any());
	}

	// ✅ DIGITAL SUBSCRIPTION → normal email
	@Test
	void sendBulkMail_digitalSubscription_success() {

		when(userSubscriptionRepo.findEmailBySubscriptionType(SubscriptionType.DIGITAL))
				.thenReturn(List.of("digital@test.com"));

		sendEmailService.sendBulkMail(MailTargetType.DIGITAL_SUBSCRIPTION, null, SUBJECT, CONTENT, null, null);

		verify(emailService, times(1)).sendEmail("digital@test.com", SUBJECT, CONTENT);
	}

	// ✅ SINGLE PURCHASE → attachment email
	@Test
	void sendBulkMail_singlePurchase_withAttachment() {

		byte[] attachment = new byte[] { 1, 2, 3 };

		when(magazinePurchaseRepo.findDistinctBuyerEmail()).thenReturn(List.of("buyer@test.com"));

		sendEmailService.sendBulkMail(MailTargetType.SINGLE_PURCHASE, null, SUBJECT, CONTENT, attachment,
				"invoice.pdf");

		verify(emailService, times(1)).sendMailWithAttachment("buyer@test.com", SUBJECT, CONTENT, attachment,
				"invoice.pdf");
	}

	// ⚠️ NO USERS → NO MAIL SENT
	@Test
	void sendBulkMail_noRecipients_shouldNotSendMail() {

		when(userRepo.findEmailByStatusAndRole(AccountStatus.BLOCKED, Role.USER)).thenReturn(List.of());

		sendEmailService.sendBulkMail(MailTargetType.ACCOUNT_STATUS, AccountStatus.BLOCKED, SUBJECT, CONTENT, null,
				null);

		verifyNoInteractions(emailService);
	}

	// ❌ MAIL FAILURE → SHOULD NOT BREAK LOOP
	@Test
	void sendBulkMail_oneMailFails_shouldContinue() {

		when(userRepo.findEmailByStatusAndRole(AccountStatus.ACTIVE, Role.USER))
				.thenReturn(List.of("a@test.com", "b@test.com"));

		doThrow(new RuntimeException("SMTP error")).when(emailService).sendEmail("a@test.com", SUBJECT, CONTENT);

		sendEmailService.sendBulkMail(MailTargetType.ACCOUNT_STATUS, AccountStatus.ACTIVE, SUBJECT, CONTENT, null,
				null);

		// both attempted
		verify(emailService, times(2)).sendEmail(anyString(), eq(SUBJECT), eq(CONTENT));
	}
}
