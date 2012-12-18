package com.tmall.top.push;

import java.util.Date;
import java.util.Hashtable;

import com.tmall.top.push.messaging.Message;

public abstract class ClientConnection {
	protected Date lastPingTime;
	protected Hashtable<String, String> headers;
	protected String id;
	protected String origin;
	protected String protocol;

	protected abstract void initHeaders();
	protected abstract void internalClear();
	
	public abstract boolean isOpen();
	public abstract void sendMessage(Message msg) throws Exception;
	
	public String getId() {
		return this.id;
	}
	public String getOrigin() {
		return this.origin;
	}
	
	public void clear() {
		this.lastPingTime = null;
		this.headers = null;
		this.internalClear();
	}

	public void init(Hashtable<String, String> headers) {
		this.ReceivePing();
		this.headers = headers;
		this.initHeaders();
	}
	
	public void ReceivePing() {
		this.lastPingTime = new Date();
	}
	
}
