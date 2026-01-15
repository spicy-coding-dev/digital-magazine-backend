package com.digital.magazine.common.exception;

public class PdfExtractionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PdfExtractionException(String message) {
		super(message);
	}

	public PdfExtractionException(String message, Throwable cause) {
		super(message, cause);
	}
}
