package com.digital.magazine.common.exception;

public class BookContentNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BookContentNotFoundException(String message) {
		super(message);
	}
}
