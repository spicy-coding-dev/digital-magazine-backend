package com.digital.magazine.common.exception;

public class InvalidBookContentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidBookContentException(String message) {
		super(message);
	}
}
