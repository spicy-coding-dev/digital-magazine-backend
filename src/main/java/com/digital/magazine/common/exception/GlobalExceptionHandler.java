package com.digital.magazine.common.exception;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.digital.magazine.common.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// ✅ Validation errors (DTO @Valid)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {

		List<String> errorMessages = ex.getBindingResult().getAllErrors().stream()
				.map(error -> error.getDefaultMessage()).collect(Collectors.toList());

		String finalMessage = String.join(", ", errorMessages);

		log.warn("Validation failed: {}", finalMessage);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(finalMessage));
	}

	// ✅ Email already exists
	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	public ResponseEntity<ApiResponse<String>> handleEmailExists(EmailAlreadyRegisteredException ex) {

		log.warn("Email already registered: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(ex.getMessage()));
	}

	// ✅ Invalid / expired token
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ApiResponse<String>> handleInvalidToken(InvalidTokenException ex) {

		log.warn("Invalid token error: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(ex.getMessage()));
	}

	@ExceptionHandler(TokenAlreadyUsedException.class)
	public ResponseEntity<ApiResponse<String>> handleTokenAlreadyUsed(TokenAlreadyUsedException ex) {

		log.warn("Token already used error: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(ex.getMessage()));
	}

	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<ApiResponse<String>> handleTokenExpired(TokenExpiredException ex) {

		log.warn("Token Expired error: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(ex.getMessage()));
	}

	// ✅ User not found
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiResponse<String>> handleUserNotFound(UserNotFoundException ex) {

		log.warn("User not found: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(ex.getMessage()));
	}

	// ✅ Unauthorized / forbidden access
	@ExceptionHandler(UnauthorizedAccessException.class)
	public ResponseEntity<ApiResponse<String>> handleUnauthorized(UnauthorizedAccessException ex) {

		log.warn("Unauthorized access attempt: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(ex.getMessage()));
	}

	// ✅ IO issues (file upload, download, etc.)
	@ExceptionHandler(IOException.class)
	public ResponseEntity<ApiResponse<String>> handleIOException(IOException ex) {

		log.error("IO Exception occurred", ex);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiResponse<>("கோப்பு செயல்பாட்டில் பிழை ஏற்பட்டுள்ளது"));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiResponse<String>> handleBadCredentials(BadCredentialsException ex) {

		log.warn("Bad credentials attempt");

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
				"உள்நுழைவதில் சிக்கல், உங்கள் மின்னஞ்சல்/மொபைல் எண் அல்லது கடவுச்சொல் சரிபார்க்கவும்"));
	}

	@ExceptionHandler(TooManyAttemptsException.class)
	public ResponseEntity<ApiResponse<String>> handleTooManyAttempts(TooManyAttemptsException ex) {

		log.warn("🚫 Too many login attempts: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(ex.getMessage()));
	}

	@ExceptionHandler(CaptchaFailedException.class)
	public ResponseEntity<ApiResponse<String>> handleCaptchaFailed(CaptchaFailedException ex) {

		log.warn("🤖 Captcha validation failed: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(ex.getMessage()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<String>> handleMissingBody(HttpMessageNotReadableException ex) {

		log.warn("Request body missing or invalid");

		return ResponseEntity.badRequest().body(new ApiResponse<>("Request body அனுப்பப்படவில்லை அல்லது தவறான format"));
	}

	// 🔴 FINAL catch-all (never expose internal error)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {

		log.error("Unhandled exception occurred", ex);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiResponse<>("உள் சேவையக பிழை. பிறகு முயற்சிக்கவும்"));
	}
}
