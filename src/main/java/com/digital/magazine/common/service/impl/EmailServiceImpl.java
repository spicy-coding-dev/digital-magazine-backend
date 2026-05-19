package com.digital.magazine.common.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.digital.magazine.common.service.EmailService;
import com.digital.magazine.subscription.entity.UserAddress;
import com.digital.magazine.subscription.enums.SubscriptionType;

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

//			String body = "வணக்கம்,\n\n" + "நீங்கள் எங்கள் டிஜிட்டல் இதழ் தளத்தில் பதிவு செய்ததற்கு நன்றி.\n\n"
//					+ "உங்கள் மின்னஞ்சலை உறுதிப்படுத்த கீழே உள்ள இணைப்பை கிளிக் செய்யவும்:\n\n" + verificationLink
//					+ "\n\n" + "இந்த இணைப்பு 15 நிமிடங்கள் மட்டுமே செல்லுபடியாகும்.\n\n"
//					+ "நீங்கள் இந்த பதிவு செய்யவில்லை என்றால், இந்த மின்னஞ்சலை பொருட்படுத்த வேண்டாம்.\n\n" + "நன்றி,\n"
//					+ "டிஜிட்டல் தமிழ் இதழ் குழு";

			String body = """
					<!DOCTYPE html>
					<html lang="ta">
					<head>
					    <meta charset="UTF-8">

					    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+Tamil:wght@400;500;600;700&display=swap" rel="stylesheet">

					</head>

					<body style="
					    font-family: 'Noto Sans Tamil', sans-serif;
					    background:#f4f4f4;
					    padding:20px;
					    margin:0;
					">

					    <div style="
					        max-width:600px;
					        margin:auto;
					        background:white;
					        padding:35px;
					        border-radius:14px;
					        box-shadow:0 4px 20px rgba(0,0,0,0.08);
					    ">

					        <h2 style="
					            color:#1e293b;
					            text-align:center;
					            margin-bottom:25px;
					            font-size:28px;
					            font-weight:700;
					        ">
					            📚 டிஜிட்டல் தமிழ் இதழ்
					        </h2>

					        <p style="
					            font-size:17px;
					            color:#222;
					            line-height:1.8;
					        ">
					            வணக்கம்,
					        </p>

					        <p style="
					            font-size:16px;
					            color:#444;
					            line-height:1.9;
					        ">
					            நீங்கள் எங்கள் டிஜிட்டல் இதழ் தளத்தில் பதிவு செய்ததற்கு நன்றி.
					        </p>

					        <p style="
					            font-size:16px;
					            color:#444;
					            line-height:1.9;
					        ">
					            உங்கள் மின்னஞ்சலை உறுதிப்படுத்த கீழே உள்ள பட்டனை கிளிக் செய்யவும்:
					        </p>

					        <div style="text-align:center; margin:35px 0;">

					            <a href="%s"
					               style="
					                    background:linear-gradient(135deg,#2563eb,#1d4ed8);
					                    color:white;
					                    padding:15px 30px;
					                    text-decoration:none;
					                    border-radius:10px;
					                    display:inline-block;
					                    font-size:16px;
					                    font-weight:600;
					                    letter-spacing:0.3px;
					               ">
					                மின்னஞ்சலை உறுதிப்படுத்து
					            </a>

					        </div>

					        <p style="
					            color:#555;
					            font-size:15px;
					            line-height:1.8;
					        ">
					            ⏰ இந்த இணைப்பு 15 நிமிடங்கள் மட்டுமே செல்லுபடியாகும்.
					        </p>

					        <p style="
					            color:#777;
					            font-size:14px;
					            line-height:1.8;
					        ">
					            நீங்கள் இந்த பதிவு செய்யவில்லை என்றால்,
					            இந்த மின்னஞ்சலை பொருட்படுத்த வேண்டாம்.
					        </p>

					        <hr style="
					            margin:30px 0;
					            border:none;
					            border-top:1px solid #e5e7eb;
					        ">

					        <p style="
					            text-align:center;
					            color:#666;
					            font-size:15px;
					            line-height:1.8;
					        ">
					            நன்றி,<br>
					            <b style="color:#111827;">
					                டிஜிட்டல் தமிழ் இதழ் குழு
					            </b>
					        </p>

					    </div>

					</body>
					</html>
					"""
					.formatted(verificationLink);

			sendMail(toEmail, subject, body);

			log.info("Verification email sent successfully to {}", toEmail);

		} catch (Exception e) {
			log.error("Failed to send verification email to {}", toEmail, e);
		}
	}

	@Async("taskExecutor")
	@Override
	public void sendVerificationEmailFromAdminAndSuper(String toEmail, String token) {

		log.info("Sending verification email to {}", toEmail);

		try {
			String verificationLink = backendBaseUrl + "/admin/verify-email?token=" + token;

			String subject = "📩 உங்கள் மின்னஞ்சல் உறுதிப்படுத்தல்";

			String body = "வணக்கம்,\n\n" + "நீங்கள் எங்கள் டிஜிட்டல் இதழ் தளத்தில் பதிவு செய்ததற்கு நன்றி.\n\n"
					+ "உங்கள் மின்னஞ்சலை உறுதிப்படுத்த கீழே உள்ள இணைப்பை கிளிக் செய்யவும்:\n\n" + verificationLink
					+ "\n\n" + "இந்த இணைப்பு 15 நிமிடங்கள் மட்டுமே செல்லுபடியாகும்.\n\n"
					+ "நீங்கள் இந்த பதிவு செய்யவில்லை என்றால், இந்த மின்னஞ்சலை பொருட்படுத்த வேண்டாம்.\n\n" + "நன்றி,\n"
					+ "டிஜிட்டல் தமிழ் இதழ் குழு";

			sendMail(toEmail, subject, body);

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

			sendMail(toEmail, subject, body);

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

			String body = """
					<!DOCTYPE html>
					<html lang="ta">

					<head>
					    <meta charset="UTF-8">

					    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+Tamil:wght@400;500;600;700&display=swap" rel="stylesheet">
					</head>

					<body style="
					    margin:0;
					    padding:20px;
					    background:#f4f4f4;
					    font-family:'Noto Sans Tamil', sans-serif;
					">

					    <div style="
					        max-width:600px;
					        margin:auto;
					        background:#ffffff;
					        border-radius:14px;
					        padding:35px;
					        box-shadow:0 4px 20px rgba(0,0,0,0.08);
					    ">

					        <h2 style="
					            text-align:center;
					            color:#1e293b;
					            font-size:28px;
					            margin-bottom:25px;
					            font-weight:700;
					        ">
					            🔐 கடவுச்சொல் மாற்றம்
					        </h2>

					        <p style="
					            font-size:16px;
					            color:#333;
					            line-height:1.9;
					        ">
					            வணக்கம்,
					        </p>

					        <p style="
					            font-size:16px;
					            color:#444;
					            line-height:1.9;
					        ">
					            உங்கள் கணக்கிற்கான கடவுச்சொல் மாற்ற கோரிக்கை பெறப்பட்டுள்ளது.
					        </p>

					        <p style="
					            font-size:16px;
					            color:#444;
					            line-height:1.9;
					        ">
					            கடவுச்சொல்லை மாற்ற கீழே உள்ள பட்டனை கிளிக் செய்யவும்:
					        </p>

					        <div style="
					            text-align:center;
					            margin:35px 0;
					        ">

					            <a href="%s"
					               style="
					                    background:linear-gradient(135deg,#dc2626,#b91c1c);
					                    color:white;
					                    text-decoration:none;
					                    padding:15px 30px;
					                    border-radius:10px;
					                    display:inline-block;
					                    font-size:16px;
					                    font-weight:600;
					               ">
					                கடவுச்சொல்லை மாற்றவும்
					            </a>

					        </div>

					        <p style="
					            color:#555;
					            font-size:15px;
					            line-height:1.8;
					        ">
					            ⏰ இந்த இணைப்பு 15 நிமிடங்கள் மட்டுமே செல்லுபடியாகும்.
					        </p>

					        <p style="
					            color:#777;
					            font-size:14px;
					            line-height:1.8;
					        ">
					            இந்த கோரிக்கை நீங்கள் செய்யவில்லை என்றால்,
					            தயவுசெய்து இந்த மின்னஞ்சலை புறக்கணிக்கவும்.
					        </p>

					        <hr style="
					            margin:30px 0;
					            border:none;
					            border-top:1px solid #e5e7eb;
					        ">

					        <p style="
					            text-align:center;
					            color:#666;
					            font-size:15px;
					            line-height:1.8;
					        ">
					            நன்றி,<br>

					            <b style="color:#111827;">
					                டிஜிட்டல் தமிழ் இதழ் குழு
					            </b>
					        </p>

					    </div>

					</body>

					</html>
					"""
					.formatted(resetLink);

			sendMail(toEmail, subject, body);

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

			String body = """
					<!DOCTYPE html>
					<html lang="ta">

					<head>
					    <meta charset="UTF-8">

					    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+Tamil:wght@400;500;600;700&display=swap" rel="stylesheet">
					</head>

					<body style="
					    margin:0;
					    padding:20px;
					    background:#f4f4f4;
					    font-family:'Noto Sans Tamil', sans-serif;
					">

					    <div style="
					        max-width:600px;
					        margin:auto;
					        background:#ffffff;
					        border-radius:14px;
					        padding:35px;
					        box-shadow:0 4px 20px rgba(0,0,0,0.08);
					    ">

					        <h2 style="
					            text-align:center;
					            color:#b91c1c;
					            font-size:28px;
					            margin-bottom:25px;
					            font-weight:700;
					        ">
					            🚫 கணக்கு தற்காலிகமாக முடக்கப்பட்டுள்ளது
					        </h2>

					        <p style="
					            font-size:16px;
					            color:#333;
					            line-height:1.9;
					        ">
					            வணக்கம்,
					        </p>

					        <p style="
					            font-size:16px;
					            color:#444;
					            line-height:1.9;
					        ">
					            உங்கள் கணக்கு நிர்வாகியின் மூலம் தற்காலிகமாக முடக்கப்பட்டுள்ளது.
					        </p>

					        <div style="
					            background:#fef2f2;
					            border-left:5px solid #dc2626;
					            padding:18px;
					            border-radius:10px;
					            margin:25px 0;
					        ">

					            <p style="
					                margin:0;
					                font-size:16px;
					                color:#991b1b;
					                font-weight:600;
					            ">
					                🔍 முடக்கப்பட்ட காரணம்:
					            </p>

					            <p style="
					                margin-top:10px;
					                color:#444;
					                font-size:15px;
					                line-height:1.8;
					            ">
					                %s
					            </p>

					        </div>

					        <p style="
					            font-size:15px;
					            color:#555;
					            line-height:1.9;
					        ">
					            இந்த பிரச்சனை குறித்து மேலதிக விளக்கம் அல்லது உதவி தேவைப்பட்டால்,
					            தயவுசெய்து நிர்வாகியை தொடர்பு கொள்ளவும்.
					        </p>

					        <hr style="
					            margin:30px 0;
					            border:none;
					            border-top:1px solid #e5e7eb;
					        ">

					        <p style="
					            text-align:center;
					            color:#666;
					            font-size:15px;
					            line-height:1.8;
					        ">
					            நன்றி,<br>

					            <b style="color:#111827;">
					                டிஜிட்டல் தமிழ் இதழ் குழு
					            </b>
					        </p>

					    </div>

					</body>

					</html>
					"""
					.formatted(reason);

			sendMail(toEmail, subject, body);

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

			String body = """
					<!DOCTYPE html>
					<html lang="ta">

					<head>
					    <meta charset="UTF-8">

					    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+Tamil:wght@400;500;600;700&display=swap" rel="stylesheet">
					</head>

					<body style="
					    margin:0;
					    padding:20px;
					    background:#f4f4f4;
					    font-family:'Noto Sans Tamil', sans-serif;
					">

					    <div style="
					        max-width:600px;
					        margin:auto;
					        background:#ffffff;
					        border-radius:14px;
					        padding:35px;
					        box-shadow:0 4px 20px rgba(0,0,0,0.08);
					    ">

					        <h2 style="
					            text-align:center;
					            color:#15803d;
					            font-size:28px;
					            margin-bottom:25px;
					            font-weight:700;
					        ">
					            ✅ கணக்கு மீண்டும் செயல்படுத்தப்பட்டது
					        </h2>

					        <p style="
					            font-size:16px;
					            color:#333;
					            line-height:1.9;
					        ">
					            வணக்கம்,
					        </p>

					        <p style="
					            font-size:16px;
					            color:#444;
					            line-height:1.9;
					        ">
					            உங்கள் கணக்கு மீண்டும் நிர்வாகியால் வெற்றிகரமாக செயல்படுத்தப்பட்டுள்ளது.
					        </p>

					        <div style="
					            background:#ecfdf5;
					            border-left:5px solid #22c55e;
					            padding:18px;
					            border-radius:10px;
					            margin:25px 0;
					        ">

					            <p style="
					                margin:0;
					                font-size:16px;
					                color:#166534;
					                font-weight:600;
					            ">
					                🔍 மீண்டும் செயல்படுத்த காரணம்:
					            </p>

					            <p style="
					                margin-top:10px;
					                color:#444;
					                font-size:15px;
					                line-height:1.8;
					            ">
					                %s
					            </p>

					        </div>

					        <p style="
					            font-size:15px;
					            color:#555;
					            line-height:1.9;
					        ">
					            இப்போது நீங்கள் உங்கள் கணக்கில் உள்நுழைந்து
					            சேவைகளை பயன்படுத்தலாம்.
					        </p>

					        <p style="
					            font-size:15px;
					            color:#555;
					            line-height:1.9;
					        ">
					            எந்தவொரு உதவி தேவைப்பட்டாலும்
					            எங்களை தொடர்பு கொள்ளுங்கள்.
					        </p>

					        <hr style="
					            margin:30px 0;
					            border:none;
					            border-top:1px solid #e5e7eb;
					        ">

					        <p style="
					            text-align:center;
					            color:#666;
					            font-size:15px;
					            line-height:1.8;
					        ">
					            நன்றி,<br>

					            <b style="color:#111827;">
					                டிஜிட்டல் தமிழ் இதழ் குழு
					            </b>
					        </p>

					    </div>

					</body>

					</html>
					"""
					.formatted(reason);

			sendMail(toEmail, subject, body);

			log.info("User unblocked mail sent successfully to {}", toEmail);

		} catch (Exception e) {
			log.error("Failed to send user unblocked mail to {}", toEmail, e);
		}
	}

	@Async("taskExecutor")
	@Override
	public void sendDigitalSubscriptionBuyMail(String toEmail, SubscriptionType planType, String userName,
			LocalDate startDate, LocalDate endDate) {

		log.info("Sending digital subscription activation mail to {}", toEmail);

		try {

			String subject = "🎉 உங்கள் சந்தா வெற்றிகரமாக செயல்படுத்தப்பட்டது | Digital Magazine";

			String body = """
					      <!DOCTYPE html>
					      <html lang="ta">

					      <head>
					          <meta charset="UTF-8">

					          <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+Tamil:wght@400;500;600;700&display=swap" rel="stylesheet">
					      </head>

					      <body style="
					          margin:0;
					          padding:20px;
					          background:#f4f4f4;
					          font-family:'Noto Sans Tamil', sans-serif;
					      ">

					          <div style="
					              max-width:600px;
					              margin:auto;
					              background:#ffffff;
					              border-radius:16px;
					              padding:35px;
					              box-shadow:0 4px 20px rgba(0,0,0,0.08);
					          ">

					              <h2 style="
					                  text-align:center;
					                  color:#2563eb;
					                  font-size:30px;
					                  margin-bottom:25px;
					                  font-weight:700;
					              ">
					                  🎉 சந்தா வெற்றிகரமாக செயல்படுத்தப்பட்டது
					              </h2>

					              <p style="
					                  font-size:16px;
					                  color:#333;
					                  line-height:1.9;
					              ">
					                  வணக்கம் <b>%s</b>,
					              </p>

					              <p style="
					                  font-size:16px;
					                  color:#444;
					                  line-height:1.9;
					              ">
					                  உங்கள்
					                  <b style="color:#2563eb;">"%s"</b>
					                  சந்தா வெற்றிகரமாக செயல்படுத்தப்பட்டுள்ளது. 🎉
					              </p>

					              <div style="
					                  background:#eff6ff;
					                  border-left:5px solid #2563eb;
					                  padding:20px;
					                  border-radius:12px;
					                  margin:30px 0;
					              ">

					                  <p style="
					                      margin:0 0 12px 0;
					                      color:#1e3a8a;
					                      font-size:16px;
					                      font-weight:600;
					                  ">
					                      📅 சந்தா விவரங்கள்
					                  </p>

					                  <p style="
					                      margin:8px 0;
					                      color:#444;
					                      font-size:15px;
					                  ">
					                      <b>தொடக்க தேதி:</b> %s
					                  </p>

					                  <p style="
					                      margin:8px 0;
					                      color:#444;
					                      font-size:15px;
					                  ">
					                      <b>முடிவு தேதி:</b> %s
					                  </p>

					              </div>

					              <p style="
					                  font-size:15px;
					                  color:#555;
					                  line-height:1.9;
					              ">
					                  இப்போது நீங்கள் Digital Magazine உள்ளடக்கங்களை
					                  முழுமையாக பயன்படுத்தலாம்.
					              </p>

					              <p style="
					                  font-size:15px;
					                  color:#555;
					                  line-height:1.9;
					              ">
					                  எங்களை தேர்வு செய்ததற்கு நன்றி 🙏
					              </p>

					              <hr style="
					                  margin:30px 0;
					                  border:none;
					                  border-top:1px solid #e5e7eb;
					              ">

					              <p style="
					                  text-align:center;
					                  color:#666;
					                  font-size:15px;
					                  line-height:1.8;
					              ">
					                  அன்புடன்,<br>

					                  <b style="color:#111827;">
					                      Digital Magazine Team
					                  </b>
					              </p>

					          </div>

					      </body>

					      </html>
					"""
					.formatted(userName, planType, startDate, endDate);

			sendMail(toEmail, subject, body);

			log.info("Digital subscription activation mail sent successfully to {}", toEmail);

		} catch (Exception e) {
			log.error("Failed to send digital subscription activation mail to {}", toEmail, e);
		}

	}

	@Async("taskExecutor")
	@Override
	public void sendPrintSubscriptionBuyMail(String toEmail, SubscriptionType planType, String userName,
			UserAddress address, LocalDate startDate, LocalDate endDate) {

		log.info("Sending print subscription activation mail to {}", toEmail);

		try {

			String subject = "🎉 உங்கள் சந்தா வெற்றிகரமாக செயல்படுத்தப்பட்டது | Digital Magazine";

			String body = """
										<!DOCTYPE html>
										<html lang="ta">

										<head>
										    <meta charset="UTF-8">

										    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+Tamil:wght@400;500;600;700&display=swap" rel="stylesheet">
										</head>

										<body style="
										    margin:0;
										    padding:20px;
										    background:#f4f4f4;
										    font-family:'Noto Sans Tamil', sans-serif;
										">

										    <div style="
										        max-width:600px;
										        margin:auto;
										        background:#ffffff;
										        border-radius:16px;
										        padding:35px;
										        box-shadow:0 4px 20px rgba(0,0,0,0.08);
										    ">

										        <h2 style="
										            text-align:center;
										            color:#2563eb;
										            font-size:30px;
										            margin-bottom:25px;
										            font-weight:700;
										        ">
										            🎉 சந்தா வெற்றிகரமாக செயல்படுத்தப்பட்டது
										        </h2>

										        <p style="
										            font-size:16px;
										            color:#333;
										            line-height:1.9;
										        ">
										            வணக்கம் <b>%s</b>,
										        </p>

										        <p style="
										            font-size:16px;
										            color:#444;
										            line-height:1.9;
										        ">
										            உங்கள்
										            <b style="color:#2563eb;">"%s"</b>
										            சந்தா வெற்றிகரமாக செயல்படுத்தப்பட்டுள்ளது. 🎉
										        </p>

										        <div style="
										            background:#eff6ff;
										            border-left:5px solid #2563eb;
										            padding:20px;
										            border-radius:12px;
										            margin:30px 0;
										        ">

										            <p style="
										                margin:0 0 12px 0;
										                color:#1e3a8a;
										                font-size:16px;
										                font-weight:600;
										            ">
										                📅 சந்தா விவரங்கள்
										            </p>

										            <p style="
										                margin:8px 0;
										                color:#444;
										                font-size:15px;
										            ">
										                <b>தொடக்க தேதி:</b> %s
										            </p>

										            <p style="
										                margin:8px 0;
										                color:#444;
										                font-size:15px;
										            ">
										                <b>முடிவு தேதி:</b> %s
										            </p>

										        </div>

										        <div style="
										            background:#f9fafb;
										            border-left:5px solid #0f766e;
										            padding:20px;
										            border-radius:12px;
										            margin:30px 0;
										        ">

										            <p style="
										                margin:0 0 12px 0;
										                color:#115e59;
										                font-size:16px;
										                font-weight:600;
										            ">
										                📦 அச்சு இதழ் விநியோகம்
										            </p>

										            <p style="
										                margin:0;
										                color:#444;
										                font-size:15px;
										                line-height:1.9;
										            ">
										                உங்கள் இதழ்கள் விரைவில் வழங்கப்பட்ட முகவரிக்கு அனுப்பப்படும்.
										            </p>

										            <p style="
										                margin-top:15px;
										                color:#111827;
										                font-size:15px;
										                line-height:1.8;
										            ">
										                <b>📍 விநியோக முகவரி:</b><br><br>

					🏠 <b>%s</b><br>

					%s<br>

					%s, %s - %s<br>
										            </p>

										        </div>

										        <p style="
										            font-size:15px;
										            color:#555;
										            line-height:1.9;
										        ">
										            எங்களை தேர்வு செய்ததற்கு நன்றி 🙏
										        </p>

										        <hr style="
										            margin:30px 0;
										            border:none;
										            border-top:1px solid #e5e7eb;
										        ">

										        <p style="
										            text-align:center;
										            color:#666;
										            font-size:15px;
										            line-height:1.8;
										        ">
										            அன்புடன்,<br>

										            <b style="color:#111827;">
										                Digital Magazine Team
										            </b>
										        </p>

										    </div>

										</body>

										</html>
										"""
					.formatted(userName, planType, startDate, endDate,

							address.getName(), address.getAddressLine(), address.getCity(), address.getState(),
							address.getPincode());

			sendMail(toEmail, subject, body);

			log.info("Print subscription activation mail sent successfully to {}", toEmail);

		} catch (Exception e) {
			log.error("Failed to send print subscription activation mail to {}", toEmail, e);
		}

	}

	@Async("taskExecutor")
	@Override
	public void sendSingleMagazineBuyMail(String toEmail, String magazineName, String userName, Long magazineNo,
			Double magazinePrice, LocalDateTime purchaseDate) {

		log.info("Sending single magazine purchase mail to {}", toEmail);

		try {

			String subject = "📘 நீங்கள் வாங்கிய இதழ் வெற்றிகரமாக திறக்கப்பட்டது | Digital Magazine";

			String body = """
					<!DOCTYPE html>
					<html lang="ta">

					<head>
					    <meta charset="UTF-8">

					    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+Tamil:wght@400;500;600;700&display=swap" rel="stylesheet">
					</head>

					<body style="
					    margin:0;
					    padding:20px;
					    background:#f4f4f4;
					    font-family:'Noto Sans Tamil', sans-serif;
					">

					    <div style="
					        max-width:600px;
					        margin:auto;
					        background:#ffffff;
					        border-radius:16px;
					        padding:35px;
					        box-shadow:0 4px 20px rgba(0,0,0,0.08);
					    ">

					        <h2 style="
					            text-align:center;
					            color:#7c3aed;
					            font-size:30px;
					            margin-bottom:25px;
					            font-weight:700;
					        ">
					            📘 இதழ் வெற்றிகரமாக வாங்கப்பட்டது
					        </h2>

					        <p style="
					            font-size:16px;
					            color:#333;
					            line-height:1.9;
					        ">
					            வணக்கம் <b>%s</b>,
					        </p>

					        <p style="
					            font-size:16px;
					            color:#444;
					            line-height:1.9;
					        ">
					            நீங்கள் வாங்கிய இதழ் வெற்றிகரமாக செயல்படுத்தப்பட்டுள்ளது. 🎉
					        </p>

					        <div style="
					            background:#f5f3ff;
					            border-left:5px solid #7c3aed;
					            padding:20px;
					            border-radius:12px;
					            margin:30px 0;
					        ">

					            <p style="
					                margin:0 0 15px 0;
					                color:#5b21b6;
					                font-size:16px;
					                font-weight:600;
					            ">
					                📖 இதழ் விவரங்கள்
					            </p>

					            <p style="
					                margin:8px 0;
					                color:#444;
					                font-size:15px;
					            ">
					                <b>இதழ் பெயர்:</b> %s
					            </p>

					            <p style="
					                margin:8px 0;
					                color:#444;
					                font-size:15px;
					            ">
					                <b>இதழ் எண்:</b> %s
					            </p>

					            <p style="
					                margin:8px 0;
					                color:#444;
					                font-size:15px;
					            ">
					                <b>கட்டணம்:</b> ₹%s
					            </p>

					            <p style="
					                margin:8px 0;
					                color:#444;
					                font-size:15px;
					            ">
					                <b>வாங்கிய தேதி:</b> %s
					            </p>

					        </div>

					        <p style="
					            font-size:15px;
					            color:#555;
					            line-height:1.9;
					        ">
					            இப்போது இந்த இதழை உங்கள் கணக்கில்
					            முழுமையாக படிக்கலாம்.
					        </p>

					        <p style="
					            font-size:15px;
					            color:#555;
					            line-height:1.9;
					        ">
					            எங்களை தேர்வு செய்ததற்கு நன்றி 🙏
					        </p>

					        <hr style="
					            margin:30px 0;
					            border:none;
					            border-top:1px solid #e5e7eb;
					        ">

					        <p style="
					            text-align:center;
					            color:#666;
					            font-size:15px;
					            line-height:1.8;
					        ">
					            அன்புடன்,<br>

					            <b style="color:#111827;">
					                Digital Magazine Team
					            </b>
					        </p>

					    </div>

					</body>

					</html>
					"""
					.formatted(userName, magazineName, magazineNo, magazinePrice, purchaseDate);
			sendMail(toEmail, subject, body);

			log.info("Single magazine purchase mail sent successfully to {}", toEmail);

		} catch (Exception e) {
			log.error("Failed to send single magazine purchase mail to {}", toEmail, e);
		}

	}

	public void sendMail(String to, String subject, String content) {

		try {

			MimeMessage message = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(subject);

			helper.setText(content, true); // ✅ HTML enable

			mailSender.send(message);

			log.info("✅ Email sent | to={}", to);

		} catch (Exception e) {

			log.error("❌ Failed to send email | to={}, reason={}", to, e.getMessage(), e);

			throw new RuntimeException("Email sending failed");
		}
	}

	public void sendMailWithAttachment(String to, String subject, String content, byte[] attachmentBytes,
			String fileName) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(content);

			helper.addAttachment(fileName, new ByteArrayResource(attachmentBytes));

			mailSender.send(message);

			log.info("✅ Email with attachment sent | to={}, file={}", to, fileName);

		} catch (Exception e) {
			log.error("❌ Failed to send attachment email | to={}, reason={}", to, e.getMessage(), e);
			throw new RuntimeException("Attachment email failed");
		}
	}

}
