package com.digital.magazine.common.exception;

public class BookNotPublishedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BookNotPublishedException(String message) {
		super(message);
	}
}
