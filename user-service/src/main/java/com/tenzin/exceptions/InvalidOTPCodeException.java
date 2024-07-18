package com.tenzin.exceptions;

public class InvalidOTPCodeException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public InvalidOTPCodeException(String message) {
       super(message);
    }
}

