package com.taobao.top.push;

public class UnauthorizedException extends Exception {

	private static final long serialVersionUID = 4880759941888643621L;

	public UnauthorizedException() {
		super("unauthorized");
	}

	public UnauthorizedException(String message) {
		super(message);
	}

	public UnauthorizedException(String message, Exception innerException) {
		super(message, innerException);
	}
}
