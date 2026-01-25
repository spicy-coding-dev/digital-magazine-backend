package com.digital.magazine.common.exception;

public class DuplicateAddressException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateAddressException(String message) {
        super(message);
    }
}
