package com.digital.magazine.common.exception;

public class InvalidPaymentTypeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidPaymentTypeException() {
		super();
	}

	public InvalidPaymentTypeException(String message) {
		super(message);
	}
}
