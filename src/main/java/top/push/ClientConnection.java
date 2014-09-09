package top.push;

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
		this.connectTime = new Date();

		if (headers != null)
			this.headers = new HashMap<Object, Object>(headers);
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
		return this.headers != null ?
				new HashMap<Object, Object>(this.headers) : null;
	}

	public Object getHeader(Object key) {
		return this.headers != null ? this.headers.get(key) : null;
	}

	public Date getConnectTime() {
		return this.connectTime;
	}

	public abstract boolean isValid();

	public abstract SendStatus sendMessage(Object msg) throws Exception;
}
