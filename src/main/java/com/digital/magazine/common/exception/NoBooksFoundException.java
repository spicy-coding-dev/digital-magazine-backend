package com.digital.magazine.common.exception;

public class NoBooksFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoBooksFoundException() {
		super();
	}

	public NoBooksFoundException(String msg) {
		super(msg);
	}

}
