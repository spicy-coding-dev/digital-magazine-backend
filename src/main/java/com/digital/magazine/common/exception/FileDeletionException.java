package com.digital.magazine.common.exception;

public class FileDeletionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileDeletionException(String message) {
		super(message);
	}

	public FileDeletionException(String message, Throwable cause) {
		super(message, cause);
	}
}
