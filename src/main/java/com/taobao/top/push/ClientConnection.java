package com.taobao.top.push;

import java.util.Date;
import java.util.HashMap;

import com.taobao.top.push.messages.Message;

public abstract class ClientConnection {
	private String id;
	
	protected Date lastPingTime;
	protected HashMap<String, String> headers;
	protected String origin;
	protected String protocol;

	protected PushManager manager;
	protected Receiver receiver;

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

	public String getProtocol() {
		return this.protocol;
	}

	public HashMap<String, String> getHeaders() {
		return this.headers;
	}

	public void clear() {
		this.lastPingTime = null;
		this.headers = null;
		this.id = null;
		this.origin = null;
		this.protocol = null;
		this.manager = null;
		this.receiver = null;
		this.internalClear();
	}

	public void init(String id, HashMap<String, String> headers, PushManager manager) {
		this.receivePing();
		this.id = id;
		this.headers = headers;
		this.manager = manager;
		this.receiver = this.manager.getReceiver();
		this.initHeaders();
	}

	public void receivePing() {
		this.lastPingTime = new Date();
	}

}
