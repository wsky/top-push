package com.tmall.top.push;

import com.tmall.top.push.messages.Message;

public abstract class ClientStateHandler {

	public abstract void onClientPending(Client client);

	public abstract void onClientIdle(Client client);

	public abstract void onClientOffline(Client client);

	public abstract void onClientOffline(Client client, Message message);
}