package com.digital.magazine.common.exception;

public class InvalidCommentReplyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidCommentReplyException(String message) {
		super(message);
	}
}