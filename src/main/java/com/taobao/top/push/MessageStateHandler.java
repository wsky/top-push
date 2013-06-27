package com.taobao.top.push;

public interface MessageStateHandler {
	
	public void onDropped(Object client, Object message, String reason);

	public void onSent(Object client, Object message);
}
