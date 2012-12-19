package com.tmall.top.websocket;

import com.tmall.top.push.Pool;

public class WebSocketClientConnectionPool extends Pool<WebSocketClientConnection>
{
	public WebSocketClientConnectionPool(int poolSize) {
		super(poolSize);
	}

	@Override
	public WebSocketClientConnection createNew() {
		return new WebSocketClientConnection();
	}
}
