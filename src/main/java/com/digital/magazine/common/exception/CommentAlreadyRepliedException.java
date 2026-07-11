package com.digital.magazine.common.exception;

public class CommentAlreadyRepliedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CommentAlreadyRepliedException(String message) {
		super(message);
	}
}