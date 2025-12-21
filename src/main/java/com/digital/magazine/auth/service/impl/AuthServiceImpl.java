package com.digital.magazine.auth.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.digital.magazine.auth.dto.RegisterRequestDto;
import com.digital.magazine.auth.entity.EmailVerificationToken;
import com.digital.magazine.auth.repository.EmailVerificationTokenRepository;
import com.digital.magazine.auth.service.AuthService;
import com.digital.magazine.common.exception.EmailAlreadyRegisteredException;
import com.digital.magazine.common.exception.InvalidTokenException;
import com.digital.magazine.common.service.EmailService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.enums.Role;
import com.digital.magazine.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional // 🔥 ADD THIS
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationTokenRepository tokenRepo;
	private final EmailService emailService;

	@Override
	public void register(RegisterRequestDto dto) {

		User existingUser = userRepo.findByEmail(dto.getEmail()).orElse(null);

		if (existingUser != null) {

			// 🔴 Case 2: Already verified user
			if (existingUser.isEmailVerified()) {
				throw new EmailAlreadyRegisteredException("இந்த மின்னஞ்சல் ஏற்கனவே பதிவு செய்யப்பட்டுள்ளது");
			}

			// 🟡 Case 3: Email exists but NOT verified
			// 👉 resend verification email
			resendVerification(existingUser);
			return;
		}

		// 🟢 Case 1: New user
		User user = User.builder().name(dto.getName()).email(dto.getEmail()).mobile(dto.getMobile())
				.password(passwordEncoder.encode(dto.getPassword())).country(dto.getCountry()).state(dto.getState())
				.district(dto.getDistrict()).role(Role.USER).status(AccountStatus.PENDING).emailVerified(false)
				.createdAt(LocalDateTime.now()).build();

		userRepo.save(user);

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

		EmailVerificationToken verificationToken = tokenRepo.findByToken(token).orElse(null);

		if (verificationToken == null) {
			throw new InvalidTokenException("தவறான அல்லது காலாவதியான இணைப்பு");
		}

		if (verificationToken.getExpiryTime().isBefore(LocalDateTime.now())) {
			return "❌ இந்த இணைப்பு காலாவதியாகிவிட்டது";
		}

		User user = verificationToken.getUser();

		if (user.isEmailVerified()) {
			return "✅ உங்கள் மின்னஞ்சல் ஏற்கனவே உறுதிப்படுத்தப்பட்டுள்ளது";
		}

		// ✅ verify user
		user.setEmailVerified(true);
		user.setStatus(AccountStatus.ACTIVE);
		userRepo.save(user);

		// 🔐 delete token (one time use)
		tokenRepo.delete(verificationToken);

		return "✅ உங்கள் மின்னஞ்சல் வெற்றிகரமாக உறுதிப்படுத்தப்பட்டது";
	}

}
