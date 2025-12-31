package com.digital.magazine.common.exception;

public class TokenExpiredException extends RuntimeException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TokenExpiredException() {
		super();
	}
	
	public TokenExpiredException(String msg) {
		super(msg);
	}

}
