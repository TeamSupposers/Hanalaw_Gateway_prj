package com.hanalaw.gateway.exception;

public class ThrottleException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ThrottleException(String message) {
		super(message);
	}
}