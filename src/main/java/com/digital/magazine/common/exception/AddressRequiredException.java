package com.digital.magazine.common.exception;

public class AddressRequiredException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddressRequiredException(String message) {
        super(message);
    }
}
