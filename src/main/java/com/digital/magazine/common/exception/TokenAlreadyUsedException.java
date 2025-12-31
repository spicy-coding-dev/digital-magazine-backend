package com.digital.magazine.common.exception;

public class TokenAlreadyUsedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TokenAlreadyUsedException() {
		super();
	}

	public TokenAlreadyUsedException(String msg) {
		super(msg);
	}

}
