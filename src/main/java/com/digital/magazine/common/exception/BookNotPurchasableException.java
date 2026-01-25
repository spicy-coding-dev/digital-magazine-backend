package com.digital.magazine.common.exception;

public class BookNotPurchasableException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BookNotPurchasableException(String message) {
		super(message);
	}
}