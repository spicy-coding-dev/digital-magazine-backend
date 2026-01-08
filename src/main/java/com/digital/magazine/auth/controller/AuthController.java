package com.digital.magazine.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.auth.dto.ForgotPasswordRequestDto;
import com.digital.magazine.auth.dto.LoginRequestDto;
import com.digital.magazine.auth.dto.RegisterRequestDto;
import com.digital.magazine.auth.dto.ResetPasswordRequestDto;
import com.digital.magazine.auth.dto.UserProfileDto;
import com.digital.magazine.auth.service.AuthService;
import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.common.response.LoginApiResponse;
import com.digital.magazine.user.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	
	@GetMapping("/me")
	public ResponseEntity<UserProfileDto> me(
	    @AuthenticationPrincipal UserDetails userDetails
	) {
	    User user = authService.findByEmail(userDetails.getUsername());

	    return ResponseEntity.ok(
	        new UserProfileDto(
	            user.getName(),
	            user.getEmail(),
	            user.getRole()
	        )
	    );
	}


	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequestDto dto) {

		authService.register(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(
				new ApiResponse<>("பதிவு வெற்றிகரமாக நிறைவடைந்தது. தயவுசெய்து உங்கள் மின்னஞ்சலை உறுதிப்படுத்தவும்."));

	}

	@GetMapping("/verify-email")
	public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {

		String result = authService.verifyEmail(token);

		return ResponseEntity.ok(new ApiResponse<>("Email verification status", result));
	}

	@PostMapping("/user-login")
	public ResponseEntity<ApiResponse<LoginApiResponse>> login(@Valid @RequestBody LoginRequestDto request,
			HttpServletResponse resp) {

		log.info("🔐 Login attempt started for key={}", request.getEmailOrPhone());

		LoginApiResponse result = authService.login(request, resp);

		log.info("✅ Login success for key={}", request.getEmailOrPhone());

		return ResponseEntity.ok(new ApiResponse<>("உள்நுழைவு வெற்றிகரமாக முடிந்தது", result));
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		LoginApiResponse apiResponse = authService.refreshToken(request);
		return ResponseEntity.ok(new ApiResponse<>("Token refresh வெற்றிகரமாக முடிந்தது", apiResponse));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto dto) {

		authService.generateResetToken(dto);

		return ResponseEntity.ok(new ApiResponse<>(
				"கடவுச்சொல் மாற்றத்திற்கான இணைப்பு (Password Reset Link) உங்கள் மின்னஞ்சலுக்கு அனுப்பப்பட்டுள்ளது. தயவுசெய்து மின்னஞ்சலை சரிபார்க்கவும்."));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto dto) {

		authService.resetPassword(dto);
		return ResponseEntity.ok(new ApiResponse<>(
				"உங்கள் கடவுச்சொல் வெற்றிகரமாக மாற்றப்பட்டுள்ளது. தயவுசெய்து புதிய கடவுச்சொல்லுடன் உள்நுழையவும்."));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {

		authService.logout(response);

		return ResponseEntity.ok(new ApiResponse<>("நீங்கள் வெற்றிகரமாக வெளியேறிவிட்டீர்கள்"));
	}

}
