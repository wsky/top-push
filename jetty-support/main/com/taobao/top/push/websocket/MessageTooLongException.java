package com.taobao.top.push.websocket;

public class MessageTooLongException extends Exception {

	private static final long serialVersionUID = 1586531324870896131L;
	
	public MessageTooLongException() {
		super("message too long");
	}
}
