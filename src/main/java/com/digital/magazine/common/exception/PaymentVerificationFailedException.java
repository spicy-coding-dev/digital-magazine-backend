package com.digital.magazine.common.exception;

public class PaymentVerificationFailedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PaymentVerificationFailedException(String msg) {
		super(msg);
	}

}
