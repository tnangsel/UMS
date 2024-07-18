package com.tenzin.exceptions;

public class MfaEnabledException extends RuntimeException {

	    private static final long serialVersionUID = 1L;

		public MfaEnabledException(String message) {
			super(message);
		}
	
}
