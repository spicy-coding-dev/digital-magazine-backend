package com.digital.magazine.user.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.digital.magazine.auth.entity.EmailVerificationToken;
import com.digital.magazine.auth.repository.EmailVerificationTokenRepository;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.common.exception.EmailAlreadyRegisteredException;
import com.digital.magazine.common.exception.InvalidTokenException;
import com.digital.magazine.common.service.EmailService;
import com.digital.magazine.user.dto.CreateUserDto;
import com.digital.magazine.user.dto.UserSetPasswordDto;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.repository.UserRepository;
import com.digital.magazine.user.service.UserCreationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCreationServiceImpl implements UserCreationService {

	private final UserRepository userRepository;
	private final EmailVerificationTokenRepository tokenRepository;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void createUser(CreateUserDto dto, Role targetRole, Authentication auth) {

		log.info("CreateUser started | requestedBy={} | targetRole={} | email={}", auth.getName(), targetRole,
				dto.getEmail());

		User creator = userRepository.findByEmail(auth.getName()).orElseThrow(() -> {
			log.error("Creator not found: {}", auth.getName());
			return new RuntimeException("Creator not found");
		});

		validatePermission(creator.getRole(), targetRole);

		// 🔹 EMAIL CHECK
		User emailUser = userRepository.findByEmail(dto.getEmail()).orElse(null);
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
		User mobileUser = userRepository.findByMobile(dto.getMobile()).orElse(null);
		if (mobileUser != null) {
			log.warn("Mobile already registered: {}", dto.getMobile());
			throw new EmailAlreadyRegisteredException("இந்த மொபைல் எண் ஏற்கனவே பதிவு செய்யப்பட்டுள்ளது");
		}

		String tempPassword = UUID.randomUUID().toString();

		User user = User.builder().name(dto.getName()).email(dto.getEmail()).mobile(dto.getMobile())
				.country(dto.getCountry()).state(dto.getState()).district(dto.getDistrict()).role(targetRole)
				.status(AccountStatus.PENDING).password(passwordEncoder.encode(tempPassword)).emailVerified(false)
				.createdAt(LocalDateTime.now()).build();

		userRepository.save(user);

		log.info("User saved with PENDING status | email={}", user.getEmail());

		sendVerification(user);
	}

	@Override
	public String verifyEmailAndSetPassword(UserSetPasswordDto dto) {

		String token = dto.getToken();

		String maskedToken = (token != null && token.length() > 6) ? token.substring(0, 6) + "***" : "***";

		log.info("Email verification attempt with token={}", maskedToken);

		EmailVerificationToken verificationToken = tokenRepository.findByToken(token).orElse(null);

		if (verificationToken == null) {
			log.warn("Invalid email verification token");
			throw new InvalidTokenException("தவறான அல்லது காலாவதியான இணைப்பு");
		}

		if (verificationToken.getExpiryTime().isBefore(LocalDateTime.now())) {
			tokenRepository.delete(verificationToken);
			throw new InvalidTokenException("இணைப்பு காலாவதியாகிவிட்டது");
		}

		User user = verificationToken.getUser();

		if (user.isEmailVerified()) {
			log.info("Email already verified for {}", user.getEmail());
			return "✅ உங்கள் மின்னஞ்சல் ஏற்கனவே உறுதிப்படுத்தப்பட்டுள்ளது";
		}

		user.setEmailVerified(true);
		user.setStatus(AccountStatus.ACTIVE);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		userRepository.save(user);

		tokenRepository.delete(verificationToken);

		log.info("Email verified successfully for {}", user.getEmail());

		return "✅ உங்கள் மின்னஞ்சல் வெற்றிகரமாக உறுதிப்படுத்தப்பட்டது";
	}

	private void validatePermission(Role creatorRole, Role targetRole) {

		if (targetRole == Role.ADMIN && creatorRole != Role.SUPER_ADMIN) {
			throw new AccessDeniedException("Only super admin can create admin");
		}

		if (targetRole == Role.USER && creatorRole == Role.USER) {
			throw new AccessDeniedException("User cannot create user");
		}
	}

	private void sendVerification(User user) {

		EmailVerificationToken token = EmailVerificationToken.builder().token(UUID.randomUUID().toString()).user(user)
				.expiryTime(LocalDateTime.now().plusMinutes(15)).build();

		tokenRepository.save(token);
		emailService.sendVerificationEmail(user.getEmail(), token.getToken());
	}

	private void resendVerification(User user) {

		// delete old token if exists
		tokenRepository.deleteByUser(user);
		tokenRepository.flush(); // 🔥 IMPORTANT

		sendVerification(user);
	}
}
