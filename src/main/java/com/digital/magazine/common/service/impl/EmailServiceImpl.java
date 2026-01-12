package com.digital.magazine.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.digital.magazine.common.service.EmailService;

import jakarta.mail.internet.MimeMessage;
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

			String resetLink = backendBaseUrl + "/reset-password?token=" + token;

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

	@Async("taskExecutor")
	@Override
	public void sendUserBlockedMail(String toEmail, String reason) {

		log.info("Sending USER BLOCKED mail to {}", toEmail);

		try {

			String subject = "🚫 உங்கள் கணக்கு தற்காலிகமாக முடக்கப்பட்டுள்ளது";

			String body = "வணக்கம்,\n\n" + "உங்கள் கணக்கு நிர்வாகியின் மூலம் தற்காலிகமாக முடக்கப்பட்டுள்ளது.\n\n"
					+ "🔍 முடக்கப்பட்ட காரணம்:\n" + reason + "\n\n"
					+ "இந்த பிரச்சனை குறித்து மேலதிக விளக்கம் அல்லது உதவி தேவைப்பட்டால்,\n"
					+ "தயவுசெய்து நிர்வாகியை தொடர்பு கொள்ளவும்.\n\n" + "நன்றி,\n" + "டிஜிட்டல் தமிழ் இதழ் குழு";

			sendEmail(toEmail, subject, body);

			log.info("User blocked mail sent successfully to {}", toEmail);

		} catch (Exception e) {
			log.error("Failed to send user blocked mail to {}", toEmail, e);
		}
	}

	@Async("taskExecutor")
	@Override
	public void sendUserUnblockedMail(String toEmail, String reason) {

		log.info("Sending USER UNBLOCKED mail to {}", toEmail);

		try {

			String subject = "✅ உங்கள் கணக்கு மீண்டும் செயல்படுத்தப்பட்டுள்ளது";

			String body = "வணக்கம்,\n\n"
					+ "உங்கள் கணக்கு மீண்டும் நிர்வாகியால் வெற்றிகரமாக செயல்படுத்தப்பட்டுள்ளது.\n\n"
					+ "🔍 மீண்டும் செயல்படுத்த காரணம்:\n" + reason + "\n\n"
					+ "இப்போது நீங்கள் உங்கள் கணக்கில் உள்நுழைந்து சேவைகளை பயன்படுத்தலாம்.\n\n"
					+ "எந்தவொரு உதவி தேவைப்பட்டாலும் எங்களை தொடர்பு கொள்ளுங்கள்.\n\n" + "நன்றி,\n"
					+ "டிஜிட்டல் தமிழ் இதழ் குழு";

			sendEmail(toEmail, subject, body);

			log.info("User unblocked mail sent successfully to {}", toEmail);

		} catch (Exception e) {
			log.error("Failed to send user unblocked mail to {}", toEmail, e);
		}
	}

	@Override
	public void sendEmail(String to, String subject, String content) {

		log.debug("📤 Preparing normal email | to={}, subject={}", to, subject);

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(content);

			mailSender.send(message);

			log.info("✅ Email sent successfully | to={}", to);

		} catch (Exception e) {
			log.error("❌ Failed to send email | to={}, reason={}", to, e.getMessage(), e);
			throw new RuntimeException("Email sending failed");
		}
	}

	@Override
	public void sendMailWithAttachment(String to, String subject, String content, MultipartFile file) {

		log.debug("📤 Preparing email with attachment | to={}, file={}", to, file.getOriginalFilename());

		try {

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(content);

			helper.addAttachment(file.getOriginalFilename(), new ByteArrayResource(file.getBytes()));

			mailSender.send(message);

			log.info("✅ Email with attachment sent | to={}, file={}", to, file.getOriginalFilename());

		} catch (Exception e) {

			log.error("❌ Failed to send email with attachment | to={}, reason={}", to, e.getMessage(), e);

			throw new RuntimeException("Email with attachment failed");
		}
	}
}
