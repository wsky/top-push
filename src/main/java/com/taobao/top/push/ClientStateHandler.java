package com.taobao.top.push;

import java.util.HashMap;

public interface ClientStateHandler {

	public Identity onClientConnecting(HashMap<String, String> headers) throws Exception;

	public void onClientDisconnect(Client client, ClientConnection clientConnection);

	public void onClientPending(Client client);

	public void onClientIdle(Client client);

	public void onClientOffline(Client client);
}