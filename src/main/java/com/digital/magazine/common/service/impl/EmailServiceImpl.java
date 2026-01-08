package com.digital.magazine.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.digital.magazine.common.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	private JavaMailSender mailSender;

	@Value("${app.backend.base-url}")
	private String backendBaseUrl;

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Async("taskExecutor")
	@Override
	public void sendVerificationEmail(String toEmail, String token) {

		log.info("Sending verification email to {}", toEmail);

		try {
			String verificationLink = backendBaseUrl + "/verify-email?token=" + token;

			String subject = "📩 உங்கள் மின்னஞ்சல் உறுதிப்படுத்தல்";

			String body = "வணக்கம்,\n\n" + "நீங்கள் எங்கள் டிஜிட்டல் இதழ் தளத்தில் பதிவு செய்ததற்கு நன்றி.\n\n"
					+ "உங்கள் மின்னஞ்சலை உறுதிப்படுத்த கீழே உள்ள இணைப்பை கிளிக் செய்யவும்:\n\n" + verificationLink
					+ "\n\n" + "இந்த இணைப்பு 15 நிமிடங்கள் மட்டுமே செல்லுபடியாகும்.\n\n"
					+ "நீங்கள் இந்த பதிவு செய்யவில்லை என்றால், இந்த மின்னஞ்சலை பொருட்படுத்த வேண்டாம்.\n\n" + "நன்றி,\n"
					+ "டிஜிட்டல் தமிழ் இதழ் குழு";

			sendEmail(toEmail, subject, body);

			log.info("Verification email sent successfully to {}", toEmail);

		} catch (Exception e) {
			log.error("Failed to send verification email to {}", toEmail, e);
		}
	}

	@Async("taskExecutor")
	@Override
	public void sendAdminVerificationEmail(String toEmail, String token) {

		log.info("📧 Sending admin verification email to {}", toEmail);

		try {
			String verificationLink = backendBaseUrl + "/api/v1/super-admin/verify-email?token=" + token;

			String subject = "👑 நிர்வாகி (Admin) கணக்கு உறுதிப்படுத்தல் – Digital Magazine";

			String body = "வணக்கம்,\n\n" + "நீங்கள் எங்கள் Digital Tamil Magazine தளத்தில்\n"
					+ "நிர்வாகி (Admin) கணக்காக சேர்க்கப்பட்டுள்ளீர்கள்.\n\n"
					+ "உங்கள் நிர்வாகி கணக்கை (Admin Account) செயல்படுத்த\n"
					+ "கீழே உள்ள இணைப்பை கிளிக் செய்து உங்கள் மின்னஞ்சலை\n" + "உறுதிப்படுத்தவும்:\n\n"
					+ verificationLink + "\n\n" + "⏳ குறிப்பு:\n"
					+ "இந்த இணைப்பு 15 நிமிடங்கள் மட்டுமே செல்லுபடியாகும்.\n\n"
					+ "⚠️ நீங்கள் இந்த Admin பதிவை எதிர்பார்க்கவில்லை என்றால்,\n"
					+ "தயவுசெய்து இந்த மின்னஞ்சலை புறக்கணிக்கவும்.\n\n" + "நன்றி,\n"
					+ "Digital Tamil Magazine – Admin Team";

			sendEmail(toEmail, subject, body);

			log.info("✅ Admin verification email sent successfully to {}", toEmail);

		} catch (Exception e) {
			log.error("❌ Failed to send admin verification email to {}", toEmail, e);
		}
	}

	@Async("taskExecutor")
	@Override
	public void sendPasswordResetMail(String toEmail, String token) {

		log.info("Sending password reset email to {}", toEmail);

		try {

			String resetLink = backendBaseUrl + "/api/v1/auth/reset-password?token=" + token;

			String subject = "🔐 கடவுச்சொல் மாற்றம்";

			String body = "வணக்கம்,\n\n" + "உங்கள் கணக்கிற்கான கடவுச்சொல் மாற்ற கோரிக்கை பெறப்பட்டுள்ளது.\n\n"
					+ "கடவுச்சொல்லை மாற்ற கீழே உள்ள இணைப்பை கிளிக் செய்யவும்:\n\n" + resetLink + "\n\n"
					+ "இந்த இணைப்பு 15 நிமிடங்கள் மட்டுமே செல்லுபடியாகும்.\n\n"
					+ "இந்த கோரிக்கை நீங்கள் செய்யவில்லை என்றால், தயவுசெய்து இந்த மின்னஞ்சலை புறக்கணிக்கவும்.\n\n"
					+ "நன்றி,\n" + "டிஜிட்டல் தமிழ் இதழ் குழு";

			sendEmail(toEmail, subject, body);

			log.info("Reset password email sent successfully to {}", toEmail);

		} catch (Exception e) {
			log.error("Failed to send password reset email to {}", toEmail, e);
		}

	}

	private void sendEmail(String to, String subject, String body) {

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);

		mailSender.send(message);
	}
}
