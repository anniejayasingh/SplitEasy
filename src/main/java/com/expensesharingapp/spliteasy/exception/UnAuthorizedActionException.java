package com.expensesharingapp.spliteasy.exception;

public class UnAuthorizedActionException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnAuthorizedActionException(String resource, Object field, Object value) {
		
		super(String.format("%s not allowed to perform this action %s : '%s'", resource, field, value));
	}
}
