package com.taobao.top.push;

import java.util.Date;
import java.util.Map;

public abstract class ClientConnection {
	private Object id;
	private Object clientId;
	private Map<?, ?> headers;
	private Date connectTime;

	public ClientConnection(Object id, Map<?, ?> headers) {
		this.id = id;
		this.headers = headers;
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

	public Map<?, ?> getHeaders() {
		return this.headers;
	}

	public Date getConnectTime() {
		return this.connectTime;
	}

	public abstract boolean isOpen();

	public abstract SendStatus sendMessage(Object msg) throws Exception;

	public abstract void close(String reasonText);

	public enum SendStatus {
		SENT, DROP, RETRY
	}
}
