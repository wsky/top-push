package com.taobao.top.push;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class ClientConnection {
	private Object id;
	private Object clientId;
	private Map<Object, Object> headers;
	private Date connectTime;

	public ClientConnection(Object id, Map<?, ?> headers) {
		this.id = id;
		this.headers = new HashMap<Object, Object>(headers);
		this.connectTime = new Date();
	}

	protected void setClientId(Object id) {
		this.clientId = id;
	}

	public Object getClientId() {
		return this.clientId;
	}

	public Object getId() {
		return this.id;
	}

	public Map<Object, Object> getHeaders() {
		return new HashMap<Object, Object>(this.headers);
	}

	public Object getHeader(Object key) {
		return this.headers.get(key);
	}

	public Date getConnectTime() {
		return this.connectTime;
	}

	public abstract boolean isOpen();

	public abstract SendStatus sendMessage(Object msg) throws Exception;

	public abstract void close(String reasonText);

	public enum SendStatus {
		SENT, RETRY, IN_DOUBT
	}
}
