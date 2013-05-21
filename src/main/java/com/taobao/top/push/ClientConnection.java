package com.taobao.top.push;

import java.util.Date;
import java.util.HashMap;

public abstract class ClientConnection {
	private Identity id;
	
	protected Date lastPingTime;
	protected HashMap<String, String> headers;
	protected String origin;
	protected String protocol;

	protected abstract void initHeaders();

	protected abstract void internalClear();

	public abstract boolean isOpen();

	public abstract void sendMessage(Object msg) throws Exception;

	public Identity getId() {
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
		this.internalClear();
	}

	public void init(Identity id, HashMap<String, String> headers) {
		this.receivePing();
		this.id = id;
		this.headers = headers;
		this.initHeaders();
	}

	public void receivePing() {
		this.lastPingTime = new Date();
	}

}
