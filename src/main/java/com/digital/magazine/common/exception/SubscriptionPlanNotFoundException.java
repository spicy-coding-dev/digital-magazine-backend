package com.digital.magazine.common.exception;

public class SubscriptionPlanNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SubscriptionPlanNotFoundException(String msg) {
		super(msg);
	}

}
