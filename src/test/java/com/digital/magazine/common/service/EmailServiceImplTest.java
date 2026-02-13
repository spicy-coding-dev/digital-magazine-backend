package com.digital.magazine.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.digital.magazine.common.service.impl.EmailServiceImpl;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

	@Mock
	private JavaMailSender mailSender;

	@InjectMocks
	private EmailServiceImpl emailService;

	@BeforeEach
	void setup() {
		// Inject @Value fields manually
		ReflectionTestUtils.setField(emailService, "backendBaseUrl", "http://localhost:8080");

		ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
	}

	// ✅ sendVerificationEmail
	@Test
	void sendVerificationEmail_success() {

		doNothing().when(mailSender).send(any(SimpleMailMessage.class));

		assertDoesNotThrow(() -> emailService.sendVerificationEmail("user@test.com", "dummy-token"));

		verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
	}

	// ✅ sendPasswordResetMail
	@Test
	void sendPasswordResetMail_success() {

		doNothing().when(mailSender).send(any(SimpleMailMessage.class));

		assertDoesNotThrow(() -> emailService.sendPasswordResetMail("user@test.com", "reset-token"));

		verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
	}

	// ✅ sendEmail (core method)
	@Test
	void sendEmail_success() {

		doNothing().when(mailSender).send(any(SimpleMailMessage.class));

		emailService.sendEmail("user@test.com", "Test Subject", "Test Content");

		verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
	}

	// ❌ sendEmail failure
	@Test
	void sendEmail_failure_shouldThrowException() {

		doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> emailService.sendEmail("user@test.com", "Test Subject", "Test Content"));

		assertEquals("Email sending failed", ex.getMessage());
	}

	// ✅ sendMailWithAttachment
	@Test
	void sendMailWithAttachment_success() {

		MimeMessage mimeMessage = mock(MimeMessage.class);

		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

		doNothing().when(mailSender).send(mimeMessage);

		assertDoesNotThrow(() -> emailService.sendMailWithAttachment("user@test.com", "Invoice", "Please find attached",
				new byte[] { 1, 2, 3 }, "invoice.pdf"));

		verify(mailSender, times(1)).send(mimeMessage);
	}
}
