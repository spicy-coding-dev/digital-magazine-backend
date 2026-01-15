package com.digital.magazine.auth.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.digital.magazine.auth.dto.ForgotPasswordRequestDto;
import com.digital.magazine.auth.dto.LoginRequestDto;
import com.digital.magazine.auth.dto.RegisterRequestDto;
import com.digital.magazine.auth.dto.ResetPasswordRequestDto;
import com.digital.magazine.auth.entity.EmailVerificationToken;
import com.digital.magazine.auth.entity.PasswordResetToken;
import com.digital.magazine.auth.repository.EmailVerificationTokenRepository;
import com.digital.magazine.auth.repository.PasswordResetTokenRepository;
import com.digital.magazine.auth.service.AuthService;
import com.digital.magazine.common.captcha.CaptchaService;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.common.exception.CaptchaFailedException;
import com.digital.magazine.common.exception.EmailAlreadyRegisteredException;
import com.digital.magazine.common.exception.InvalidTokenException;
import com.digital.magazine.common.exception.TokenAlreadyUsedException;
import com.digital.magazine.common.exception.TokenExpiredException;
import com.digital.magazine.common.exception.TooManyAttemptsException;
import com.digital.magazine.common.exception.UnauthorizedAccessException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.common.response.LoginApiResponse;
import com.digital.magazine.common.security.LoginAttemptService;
import com.digital.magazine.common.service.EmailService;
import com.digital.magazine.security.jwt.JwtUtil;
import com.digital.magazine.security.service.CustomUserDetailsService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 🔥 ADD THIS
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationTokenRepository tokenRepo;
	private final PasswordResetTokenRepository passwordTokenRepo;
	private final EmailService emailService;
	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService customUserDetailsService;
	private final AuthenticationManager authManager;
	private final CaptchaService captchaService;
	private final LoginAttemptService loginAttemptService;

	@Override
	public void register(RegisterRequestDto dto) {

		log.info("Registration attempt started for email={}", dto.getEmail());

		// 🔹 EMAIL CHECK
		User emailUser = userRepo.findByEmail(dto.getEmail()).orElse(null);
		if (emailUser != null) {

			if (emailUser.isEmailVerified()) {
				log.warn("Email already registered and verified: {}", dto.getEmail());
				throw new EmailAlreadyRegisteredException("இந்த மின்னஞ்சல் ஏற்கனவே பதிவு செய்யப்பட்டுள்ளது");
			}

			log.info("Email exists but not verified. Resending verification: {}", dto.getEmail());
			resendVerification(emailUser);
			return; // 🔥 VERY IMPORTANT
		}

		// 🔹 MOBILE CHECK
		User mobileUser = userRepo.findByMobile(dto.getMobile()).orElse(null);
		if (mobileUser != null) {
			log.warn("Mobile already registered: {}", dto.getMobile());
			throw new EmailAlreadyRegisteredException("இந்த மொபைல் எண் ஏற்கனவே பதிவு செய்யப்பட்டுள்ளது");
		}

		User user = User.builder().name(dto.getName()).email(dto.getEmail()).mobile(dto.getMobile())
				.password(passwordEncoder.encode(dto.getPassword())).country(dto.getCountry()).state(dto.getState())
				.district(dto.getDistrict()).role(Role.USER).status(AccountStatus.PENDING).emailVerified(false)
				.createdAt(LocalDateTime.now()).build();

		userRepo.save(user);

		log.info("New user registered successfully. Email verification pending: {}", dto.getEmail());

		sendVerification(user);
	}

	private void sendVerification(User user) {

		EmailVerificationToken token = EmailVerificationToken.builder().token(UUID.randomUUID().toString()).user(user)
				.expiryTime(LocalDateTime.now().plusMinutes(15)).build();

		tokenRepo.save(token);
		emailService.sendVerificationEmail(user.getEmail(), token.getToken());
	}

	private void resendVerification(User user) {

		// delete old token if exists
		tokenRepo.deleteByUser(user);
		tokenRepo.flush(); // 🔥 IMPORTANT

		sendVerification(user);
	}

	public String verifyEmail(String token) {

		String maskedToken = (token != null && token.length() > 6) ? token.substring(0, 6) + "***" : "***";

		log.info("Email verification attempt with token={}", maskedToken);

		EmailVerificationToken verificationToken = tokenRepo.findByToken(token).orElse(null);

		if (verificationToken == null) {
			log.warn("Invalid email verification token");
			throw new InvalidTokenException("தவறான அல்லது காலாவதியான இணைப்பு");
		}

		if (verificationToken.getExpiryTime().isBefore(LocalDateTime.now())) {
			log.warn("Expired email verification token");
			return "❌ இந்த இணைப்பு காலாவதியாகிவிட்டது";
		}

		User user = verificationToken.getUser();

		if (user.isEmailVerified()) {
			log.info("Email already verified for {}", user.getEmail());
			return "✅ உங்கள் மின்னஞ்சல் ஏற்கனவே உறுதிப்படுத்தப்பட்டுள்ளது";
		}

		user.setEmailVerified(true);
		user.setStatus(AccountStatus.ACTIVE);
		userRepo.save(user);

		tokenRepo.delete(verificationToken);

		log.info("Email verified successfully for {}", user.getEmail());

		return "✅ உங்கள் மின்னஞ்சல் வெற்றிகரமாக உறுதிப்படுத்தப்பட்டது";
	}

	public LoginApiResponse login(LoginRequestDto request, HttpServletResponse resp) {

		String key = request.getEmailOrPhone();
		log.info("➡️ Processing login for key={}", key);

		// 1️⃣ Account status check
		checkUserAccountStatus(key);

		// Block check
		if (loginAttemptService.isBlocked(key)) {
			log.warn("🚫 Login blocked due to max attempts for key={}", key);
			throw new TooManyAttemptsException(
					"அதிக முயற்சிகள் கண்டறியப்பட்டுள்ளன. சில நிமிடங்கள் கழித்து மீண்டும் முயற்சிக்கவும்.");
		}

		// Captcha check
		if (loginAttemptService.shouldShowCaptcha(key)) {
			log.info("🤖 Captcha required for key={}", key);
			if (!captchaService.validate(request.getCaptchaResponse())) {
				log.warn("❌ Captcha validation failed for key={}", key);
				throw new CaptchaFailedException("Captcha சரிபார்ப்பு தோல்வியடைந்தது.");
			}
		}

		try {
			authManager.authenticate(new UsernamePasswordAuthenticationToken(key, request.getPassword()));
			log.info("🔑 Authentication success for key={}", key);

			loginAttemptService.loginSucceeded(key);

			UserDetails userDetails = customUserDetailsService.loadUserByUsername(key);

			User userEntity = userRepo.findByEmailOrMobile(key).orElseThrow(() -> {
				log.error("❌ User record missing after authentication for key={}", key);
				return new BadCredentialsException("Invalid login");
			});

			String accessToken = jwtUtil.generateAccessToken(userDetails.getUsername());
			String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

			ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken).httpOnly(true).path("/")
					.secure(false) // prod -> true
					.sameSite("Lax").maxAge(60 * 60 * 24 * 30).build();

			resp.addHeader("Set-Cookie", cookie.toString());
			log.info("🍪 Refresh token cookie issued for key={}", key);

			return new LoginApiResponse(accessToken, userEntity.getRole());

		} catch (BadCredentialsException ex) {
			loginAttemptService.loginFailed(key);
			log.warn("❌ Authentication failed for key={}", key);
			throw ex;
		}
	}

	public LoginApiResponse refreshToken(HttpServletRequest request) {

		log.info("🔄 Refresh token request received");

		// 🔍 1. Get refresh token from cookie
		Cookie[] cookies = request.getCookies();
		String refreshToken = null;

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("refreshToken".equals(cookie.getName())) {
					refreshToken = cookie.getValue();
					break;
				}
			}
		}

		if (refreshToken == null) {
			log.warn("🚫 Refresh token cookie not found");
			throw new UnauthorizedAccessException("Refresh Token காணப்படவில்லை.");
		}

		// 🔐 Mask token for logging
		String maskedToken = refreshToken.length() > 10 ? refreshToken.substring(0, 10) + "***" : "***";

		log.debug("🍪 Refresh token received: {}", maskedToken);

		// 🔍 2. Extract username
		String username;
		try {
			username = jwtUtil.extractUsername(refreshToken);
			log.info("👤 Username extracted from refresh token: {}", username);
		} catch (Exception e) {
			log.warn("❌ Failed to extract username from refresh token: {}", maskedToken);
			throw new UnauthorizedAccessException("Refresh Token தவறானது.");
		}

		// 🔍 3. Validate token
		if (!jwtUtil.validateToken(refreshToken, username)) {
			log.warn("⏰ Refresh token expired or invalid for user={}", username);
			throw new UnauthorizedAccessException("Refresh Token காலாவதியானது அல்லது தவறானது.");
		}

		// 🔍 4. Load user
		User userEntity = userRepo.findByEmail(username).orElseThrow(() -> {
			log.error("❌ User not found for refresh token, username={}", username);
			return new UserNotFoundException("பயனர் காணப்படவில்லை");
		});

		// 🔍 5. Generate new access token
		String newAccessToken = jwtUtil.generateAccessToken(username);
		log.info("✅ New access token generated successfully for user={}", username);

		// 🔍 6. Build response
		return new LoginApiResponse(newAccessToken, userEntity.getRole());
	}

	@Override
	public void generateResetToken(ForgotPasswordRequestDto dto) {

		log.info("🔐 Forgot password request received for key={}", dto.getEmailOrMobile());

		// 1️⃣ Account status check
		checkUserAccountStatus(dto.getEmailOrMobile());

		User user = userRepo.findByEmailOrMobile(dto.getEmailOrMobile()).orElseThrow(() -> {
			log.warn("❌ Forgot password failed - user not found for key={}", dto.getEmailOrMobile());
			return new UserNotFoundException("இந்த மின்னஞ்சல் / மொபைல் எண்ணுடன் எந்த பயனரும் இல்லை");
		});

		String token = UUID.randomUUID().toString();

		PasswordResetToken resetToken = PasswordResetToken.builder().token(token).user(user)
				.expiryTime(LocalDateTime.now().plusMinutes(15)).used(false).build();

		passwordTokenRepo.save(resetToken);

		// 🔐 Mask token for logs
		String maskedToken = token.substring(0, 8) + "***";

		log.debug("🔑 Password reset token generated for email={}, token={}", user.getEmail(), maskedToken);

		emailService.sendPasswordResetMail(user.getEmail(), token);

		log.info("📧 Password reset link sent successfully to email={}", user.getEmail());
	}

	public void resetPassword(ResetPasswordRequestDto dto) {

		log.info("🔄 Password reset attempt started");

		PasswordResetToken resetToken = passwordTokenRepo.findByToken(dto.getToken()).orElseThrow(() -> {
			log.warn("❌ Invalid password reset token received");
			return new InvalidTokenException("தவறான அல்லது செல்லாத Reset Token");
		});

		if (resetToken.isUsed()) {
			log.warn("⚠️ Password reset attempted with already used token for email={}",
					resetToken.getUser().getEmail());
			throw new TokenAlreadyUsedException("இந்த Reset Link ஏற்கனவே பயன்படுத்தப்பட்டுள்ளது");
		}

		if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
			log.warn("⏰ Expired password reset token for email={}", resetToken.getUser().getEmail());
			throw new TokenExpiredException("Reset Link காலாவதியானது");
		}

		User user = resetToken.getUser();

		log.info("👤 Resetting password for user={}", user.getEmail());

		user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
		userRepo.save(user);

		resetToken.setUsed(true);
		passwordTokenRepo.save(resetToken);

		log.info("✅ Password reset successful for user={}", user.getEmail());
	}

	@Override
	public void logout(HttpServletResponse response) {

		// 🔹 Clear refresh token cookie
		ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "").httpOnly(true).secure(false) // prod =
																											// true
				.path("/").maxAge(0).build();

		response.addHeader("Set-Cookie", deleteCookie.toString());

		log.info("🚪 User logged out successfully");
	}

	@Override
	public User findByEmail(String email) {

		return userRepo.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
	}

	private void checkUserAccountStatus(String userEmailOrMobile) {

		User userData = userRepo.findByEmailOrMobile(userEmailOrMobile)
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		switch (userData.getStatus()) {

		case BLOCKED -> throw new UnauthorizedAccessException(
				"உங்கள் கணக்கு தற்காலிகமாக முடக்கப்பட்டுள்ளது நிர்வாகத்தை தொடர்பு கொள்ளவும்.");

		case PENDING -> throw new UnauthorizedAccessException(
				"உங்கள் கணக்கு Pendingல் உள்ளது. தயவுசெய்து உங்கள் மின்னஞ்சலை உறுதிபடுத்தவும்.");

		case ACTIVE -> {
			// ACTIVE account → nothing to do
			return;
		}

		default -> {
			return; // fallback
		}
		}
	}

}
