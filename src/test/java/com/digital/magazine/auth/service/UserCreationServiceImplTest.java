package com.digital.magazine.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

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
import com.digital.magazine.user.service.impl.UserCreationServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserCreationServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private EmailVerificationTokenRepository tokenRepository;

	@Mock
	private EmailService emailService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private UserCreationServiceImpl service;

	private User superAdmin;
	private CreateUserDto createDto;

	@BeforeEach
	void setup() {

		superAdmin = User.builder().id(1L).email("super@test.com").role(Role.SUPER_ADMIN).build();

		createDto = CreateUserDto.builder().name("Test User").email("user@test.com").mobile("9876543210")
				.country("India").state("TN").district("Madurai").build();

//		when(authentication.getName()).thenReturn("super@test.com");
	}

	// ===================== createUser =====================

	@Test
	void createUser_success() {

		// 🔥 THIS IS THE KEY FIX
		when(authentication.getName()).thenReturn("super@test.com");

		when(userRepository.findByEmail("super@test.com")).thenReturn(Optional.of(superAdmin));

		when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

		when(userRepository.findByMobile("9876543210")).thenReturn(Optional.empty());

		when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");

		service.createUser(createDto, Role.USER, authentication);

		verify(userRepository).save(any(User.class));
		verify(tokenRepository).save(any(EmailVerificationToken.class));
		verify(emailService).sendVerificationEmail(eq("user@test.com"), anyString());
	}

	@Test
	void createUser_emailAlreadyVerified_shouldThrowException() {

		when(authentication.getName()).thenReturn("super@test.com");

		User existing = User.builder().email("user@test.com").emailVerified(true).build();

		when(userRepository.findByEmail("super@test.com")).thenReturn(Optional.of(superAdmin));

		when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(existing));

		assertThrows(EmailAlreadyRegisteredException.class,
				() -> service.createUser(createDto, Role.USER, authentication));
	}

	@Test
	void createUser_emailExistsNotVerified_shouldResendVerification() {

		// 🔥 THIS IS THE FIX
		when(authentication.getName()).thenReturn("super@test.com");

		User existing = User.builder().email("user@test.com").emailVerified(false).build();

		when(userRepository.findByEmail("super@test.com")).thenReturn(Optional.of(superAdmin));

		when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(existing));

		service.createUser(createDto, Role.USER, authentication);

		verify(tokenRepository).deleteByUser(existing);
		verify(tokenRepository).flush();
		verify(emailService).sendVerificationEmail(eq("user@test.com"), anyString());

		verify(userRepository, never()).save(any());
	}

	@Test
	void createUser_mobileAlreadyExists_shouldThrowException() {

		// 🔥 THIS IS THE FIX
		when(authentication.getName()).thenReturn("super@test.com");

		when(userRepository.findByEmail("super@test.com")).thenReturn(Optional.of(superAdmin));

		when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

		when(userRepository.findByMobile("9876543210")).thenReturn(Optional.of(new User()));

		assertThrows(EmailAlreadyRegisteredException.class,
				() -> service.createUser(createDto, Role.USER, authentication));
	}

	@Test
	void createUser_permissionDenied_shouldThrowAccessDenied() {

		when(authentication.getName()).thenReturn("super@test.com");

		User normalUser = User.builder().email("user1@test.com").role(Role.USER).build();

		when(userRepository.findByEmail("super@test.com")).thenReturn(Optional.of(normalUser));

		assertThrows(AccessDeniedException.class, () -> service.createUser(createDto, Role.USER, authentication));
	}

	// ===================== verifyEmailAndSetPassword =====================

	@Test
	void verifyEmail_invalidToken_shouldThrowException() {

		when(tokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

		UserSetPasswordDto dto = UserSetPasswordDto.builder().token("invalid").password("New@123").build();

		assertThrows(InvalidTokenException.class, () -> service.verifyEmailAndSetPassword(dto));
	}

	@Test
	void verifyEmail_expiredToken_shouldThrowException() {

		EmailVerificationToken token = EmailVerificationToken.builder().token("expired")
				.expiryTime(LocalDateTime.now().minusMinutes(1)).build();

		when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

		UserSetPasswordDto dto = UserSetPasswordDto.builder().token("expired").password("New@123").build();

		assertThrows(InvalidTokenException.class, () -> service.verifyEmailAndSetPassword(dto));

		verify(tokenRepository).delete(token);
	}

	@Test
	void verifyEmail_alreadyVerified_shouldReturnMessage() {

		User user = User.builder().email("user@test.com").emailVerified(true).build();

		EmailVerificationToken token = EmailVerificationToken.builder().token("valid").user(user)
				.expiryTime(LocalDateTime.now().plusMinutes(10)).build();

		when(tokenRepository.findByToken("valid")).thenReturn(Optional.of(token));

		String result = service
				.verifyEmailAndSetPassword(UserSetPasswordDto.builder().token("valid").password("New@123").build());

		assertTrue(result.contains("ஏற்கனவே"));
	}

	@Test
	void verifyEmail_success_shouldActivateUser() {

		User user = User.builder().email("user@test.com").emailVerified(false).status(AccountStatus.PENDING).build();

		EmailVerificationToken token = EmailVerificationToken.builder().token(UUID.randomUUID().toString()).user(user)
				.expiryTime(LocalDateTime.now().plusMinutes(10)).build();

		when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

		when(passwordEncoder.encode(anyString())).thenReturn("ENCODED"); // 🔥 ONLY NEEDED HERE

		String result = service.verifyEmailAndSetPassword(
				UserSetPasswordDto.builder().token(token.getToken()).password("New@123").build());

		assertEquals(AccountStatus.ACTIVE, user.getStatus());
		assertTrue(user.isEmailVerified());

		verify(userRepository).save(user);
		verify(tokenRepository).delete(token);

		assertTrue(result.contains("வெற்றிகரமாக"));
	}

}
