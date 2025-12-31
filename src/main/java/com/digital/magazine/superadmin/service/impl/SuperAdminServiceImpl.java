package com.digital.magazine.superadmin.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.digital.magazine.auth.entity.EmailVerificationToken;
import com.digital.magazine.auth.repository.EmailVerificationTokenRepository;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.common.exception.EmailAlreadyRegisteredException;
import com.digital.magazine.common.exception.InvalidTokenException;
import com.digital.magazine.common.service.EmailService;
import com.digital.magazine.superadmin.dto.CreateAdminRequestDto;
import com.digital.magazine.superadmin.service.SuperAdminService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuperAdminServiceImpl implements SuperAdminService {

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationTokenRepository tokenRepo;
	private final EmailService emailService;

	@Override
	public void createAdmin(CreateAdminRequestDto dto) {

		log.info("👑 Super Admin creating new Admin with email={}", dto.getEmail());

		User existingAdmin = userRepo.findByEmail(dto.getEmail()).orElse(null);

		if (existingAdmin != null) {

			if (existingAdmin.isEmailVerified()) {
				log.warn("Registration failed - email already verified: {}", dto.getEmail());
				throw new EmailAlreadyRegisteredException("இந்த மின்னஞ்சல் ஏற்கனவே பதிவு செய்யப்பட்டுள்ளது");
			}

			log.info("Email exists but not verified. Resending verification email: {}", dto.getEmail());
			resendVerification(existingAdmin);
			return;
		}

		User admin = User.builder().name(dto.getName()).email(dto.getEmail()).mobile(dto.getMobile())
				.password(passwordEncoder.encode(dto.getPassword())).country(dto.getCountry()).state(dto.getState())
				.district(dto.getDistrict()).role(Role.ADMIN).status(AccountStatus.PENDING).emailVerified(false)
				.createdAt(LocalDateTime.now()).build();

		userRepo.save(admin);

		log.info("✅ Admin created successfully with email={}", dto.getEmail());

		sendVerification(admin); // 🔥 MISSING LINE
	}

	private void sendVerification(User user) {

		EmailVerificationToken token = EmailVerificationToken.builder().token(UUID.randomUUID().toString()).user(user)
				.expiryTime(LocalDateTime.now().plusMinutes(15)).build();

		tokenRepo.save(token);
		emailService.sendAdminVerificationEmail(user.getEmail(), token.getToken());
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

}
