package com.digital.magazine.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.digital.magazine.common.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	public ResponseEntity<ApiResponse<String>> handleEmailExists(EmailAlreadyRegisteredException ex) {

		return ResponseEntity.status(HttpStatus.CONFLICT) // 409
				.body(new ApiResponse<>(ex.getMessage()));
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ApiResponse<String>> handleInvalidToken(InvalidTokenException ex) {

		return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400
				.body(new ApiResponse<>(ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<String>> handleGeneric(Exception ex) {

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiResponse<>("உள் சேவையக பிழை. பிறகு முயற்சிக்கவும்"));
	}

}
