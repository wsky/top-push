package com.taobao.top.push;

public interface MessageStateHandler {
	
	public void onDropped(Identity client, Object message, String reason);

	public void onSent(Identity client, Object message);
}
