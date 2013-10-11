package com.taobao.top.push;

import java.util.Date;
import java.util.Map;

public abstract class ClientConnection {
	private Object id;

	protected Date connectTime;
	protected Date lastPingTime;
	protected Map<String, String> headers;
	protected String origin;
	protected String protocol;

	protected abstract void initHeaders();

	protected abstract void internalClear();

	public abstract boolean isOpen();

	public abstract SendStatus sendMessage(Object msg) throws Exception;

	public abstract void close(String reasonText);

	public Object getId() {
		return this.id;
	}

	public String getOrigin() {
		return this.origin;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public Map<String, String> getHeaders() {
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

	public void init(Object id, Map<String, String> headers) {
		this.connectTime = new Date();
		this.receivePing();
		this.id = id;
		this.headers = headers;
		this.initHeaders();
	}

	public Date getConnectTime() {
		return this.connectTime;
	}

	public void receivePing() {
		this.lastPingTime = new Date();
	}

	public Date getLastPingTime() {
		return this.lastPingTime;
	}

	public enum SendStatus {
		SENT, DROP, RETRY
	}
}
