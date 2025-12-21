package com.digital.magazine.common.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public EmailAlreadyRegisteredException(){
		super();
	}
	
	public EmailAlreadyRegisteredException(String msg){
		super(msg);
	}

}
