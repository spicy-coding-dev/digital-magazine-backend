package com.digital.magazine.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

	private boolean success;
	private String message;
	private T data;

	// success response without data
	public ApiResponse(String message) {
		this.success = true;
		this.message = message;
		this.data = null;
	}

	// success response with data
	public ApiResponse(String message, T data) {
		this.success = true;
		this.message = message;
		this.data = data;
	}

	// failure response
	public static <T> ApiResponse<T> failure(String message) {
		return new ApiResponse<>(false, message, null);
	}
}
