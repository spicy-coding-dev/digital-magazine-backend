package com.digital.magazine.common.exception;

public class InvalidBookException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidBookException(String message) {
		super(message);
	}

	public InvalidBookException(String message, Throwable cause) {
		super(message, cause);
	}
}
