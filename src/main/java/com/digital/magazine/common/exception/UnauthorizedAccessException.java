package com.digital.magazine.common.exception;

public class UnauthorizedAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnauthorizedAccessException() {
		super();
	}

	public UnauthorizedAccessException(String msg) {
		super(msg);
	}

}
