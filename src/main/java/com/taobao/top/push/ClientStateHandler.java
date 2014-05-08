package com.taobao.top.push;

public interface ClientStateHandler {
	
	public void onClientConnect(Client client, ClientConnection clientConnection);

	public void onClientDisconnect(Client client, ClientConnection clientConnection,String reasonText);

	public void onClientPending(Client client);

	public void onClientIdle(Client client);

	public void onClientOffline(Client client);
}