package com.tmall.top.push;

public abstract class ClientStateHandler {
	
	public abstract void onClientPending(Client client);

	public abstract void onClientIdle(Client client);

	public abstract void onClientOffline(Client client);
}