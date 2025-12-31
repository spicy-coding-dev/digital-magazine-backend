package com.digital.magazine.common.exception;

public class TooManyAttemptsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TooManyAttemptsException() {
		super();
	}

	public TooManyAttemptsException(String msg) {
		super(msg);
	}

}
