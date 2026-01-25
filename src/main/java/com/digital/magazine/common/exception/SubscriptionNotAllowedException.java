package com.digital.magazine.common.exception;

public class SubscriptionNotAllowedException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SubscriptionNotAllowedException(String message) {
        super(message);
    }
}
