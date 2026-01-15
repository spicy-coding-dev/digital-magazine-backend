package com.digital.magazine.common.exception;

public class InvalidStatusTransitionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidStatusTransitionException() {
		super();
	}

	public InvalidStatusTransitionException(String message) {
		super(message);
	}
}
