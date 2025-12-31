package com.digital.magazine.common.exception;

public class CaptchaFailedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CaptchaFailedException() {
		super();
	}

	public CaptchaFailedException(String msg) {
		super(msg);
	}
}
