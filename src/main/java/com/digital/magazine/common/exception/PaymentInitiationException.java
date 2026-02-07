package com.digital.magazine.common.exception;

public class PaymentInitiationException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PaymentInitiationException(String message) {
        super(message);
    }

    public PaymentInitiationException(String message, Throwable cause) {
        super(message, cause);
    }
}

