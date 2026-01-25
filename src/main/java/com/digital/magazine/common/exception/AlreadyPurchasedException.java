package com.digital.magazine.common.exception;

public class AlreadyPurchasedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AlreadyPurchasedException(String message) {
		super(message);
	}
}