package com.tmall.top.push;

import java.util.HashMap;

public abstract class ClientStateHandler {
	
	public abstract String onClientConnecting(HashMap<String, String> headers);
	
	public abstract void onClientConnect(Client client, ClientConnection clientConnection) throws UnauthorizedException;
	
	public abstract void onClientDisconnect(Client client, ClientConnection clientConnection);

	public abstract void onClientPending(Client client);

	public abstract void onClientIdle(Client client);

	public abstract void onClientOffline(Client client);

	//public abstract void onClientOffline(Client client, Message message);
}