package com.digital.magazine.auth.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.digital.magazine.auth.dto.LoginRequestDto;
import com.digital.magazine.auth.dto.RegisterRequestDto;
import com.digital.magazine.auth.entity.EmailVerificationToken;
import com.digital.magazine.auth.repository.EmailVerificationTokenRepository;
import com.digital.magazine.auth.service.impl.AuthServiceImpl;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.common.exception.EmailAlreadyRegisteredException;
import com.digital.magazine.common.response.LoginApiResponse;
import com.digital.magazine.common.security.LoginAttemptService;
import com.digital.magazine.common.service.EmailService;
import com.digital.magazine.common.captcha.CaptchaService;
import com.digital.magazine.security.jwt.JwtUtil;
import com.digital.magazine.security.service.CustomUserDetailsService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@InjectMocks
	private AuthServiceImpl authService;

	@Mock
	private UserRepository userRepo;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private EmailVerificationTokenRepository tokenRepo;

	@Mock
	private EmailService emailService;

	@Mock
	private AuthenticationManager authManager;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private CustomUserDetailsService customUserDetailsService;

	@Mock
	private CaptchaService captchaService;

	@Mock
	private LoginAttemptService loginAttemptService;

	@Test
	void register_newUser_success() {

		RegisterRequestDto dto = new RegisterRequestDto();
		dto.setName("Aslam");
		dto.setEmail("test@gmail.com");
		dto.setMobile("9876543210");
		dto.setPassword("Password@123");

		when(userRepo.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

		when(userRepo.findByMobile(dto.getMobile())).thenReturn(Optional.empty());

		when(passwordEncoder.encode(any())).thenReturn("encodedPwd");

		authService.register(dto);

		verify(userRepo).save(any(User.class));
		verify(tokenRepo).save(any(EmailVerificationToken.class));
		verify(emailService).sendVerificationEmail(eq(dto.getEmail()), anyString());
	}

	@Test
	void register_emailAlreadyVerified_throwException() {

		User user = User.builder().email("test@gmail.com").emailVerified(true).build();

		when(userRepo.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

		RegisterRequestDto dto = new RegisterRequestDto();
		dto.setEmail("test@gmail.com");

		assertThrows(EmailAlreadyRegisteredException.class, () -> authService.register(dto));

	}

	@Test
	void login_wrongPassword_throwException() {

		LoginRequestDto dto = new LoginRequestDto();
		dto.setEmailOrPhone("test@gmail.com");
		dto.setPassword("wrong");

		User user = User.builder().email("test@gmail.com").status(AccountStatus.ACTIVE) // 🔥 IMPORTANT
				.build();

		when(userRepo.findByEmailOrMobile(any())).thenReturn(Optional.of(user)); // 🔥 FIX

		when(loginAttemptService.isBlocked(any())).thenReturn(false);

		when(loginAttemptService.shouldShowCaptcha(any())).thenReturn(false);

		when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad"));

		assertThrows(BadCredentialsException.class, () -> authService.login(dto, mock(HttpServletResponse.class)));

		verify(loginAttemptService).loginFailed(any());
	}

	@Test
	void refreshToken_success() {

		HttpServletRequest request = mock(HttpServletRequest.class);

		Cookie cookie = new Cookie("refreshToken", "valid-token");
		when(request.getCookies()).thenReturn(new Cookie[] { cookie });

		when(jwtUtil.extractUsername("valid-token")).thenReturn("test@gmail.com");

		when(jwtUtil.validateToken(any(), any())).thenReturn(true);

		when(jwtUtil.generateAccessToken(any())).thenReturn("new-access-token"); // 🔥 MISSING MOCK

		User user = User.builder().email("test@gmail.com").role(Role.USER).build();

		when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));

		LoginApiResponse response = authService.refreshToken(request);

		assertNotNull(response);
		assertNotNull(response.accessToken());
	}

}
