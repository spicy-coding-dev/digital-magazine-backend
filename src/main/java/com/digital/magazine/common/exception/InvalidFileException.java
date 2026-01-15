package com.digital.magazine.common.exception;

public class InvalidFileException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFileException() {
		super();
	}

	public InvalidFileException(String message) {
		super(message);
	}
}