package com.taobao.top.mix;

import java.util.HashMap;

import com.taobao.top.link.channel.ServerChannelSender;
import com.taobao.top.link.endpoint.EndpointProxy;
import com.taobao.top.push.ClientConnection;

public class ClientEndpointConnection extends ClientConnection {
	private EndpointProxy endpoint;
	private ServerChannelSender sender;

	public ClientEndpointConnection(EndpointProxy endpoint, ServerChannelSender sender) {
		this.endpoint = endpoint;
		this.sender = sender;
	}

	@Override
	protected void initHeaders() {
	}

	@Override
	protected void internalClear() {
		this.endpoint = null;
		this.sender = null;
	}

	@Override
	public boolean isOpen() {
		return this.sender.isOpen();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sendMessage(Object msg) throws Exception {
		this.endpoint.sendAndWait((HashMap<String, String>) msg, 100);
	}

	@Override
	public void close(String reasonText) {
		this.sender.close(reasonText);
	}
}
