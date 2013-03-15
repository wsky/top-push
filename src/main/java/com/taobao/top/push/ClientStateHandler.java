package com.taobao.top.push;

import java.util.HashMap;

public abstract class ClientStateHandler {
	
	public abstract String onClientConnecting(HashMap<String, String> headers) throws UnauthorizedException;
	
	public abstract void onClientDisconnect(Client client, ClientConnection clientConnection);

	public abstract void onClientPending(Client client);

	public abstract void onClientIdle(Client client);

	public abstract void onClientOffline(Client client);
}