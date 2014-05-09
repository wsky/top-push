package com.taobao.top.push;

public interface MessageSender {
	public void setConnections(ClientConnection[] connections);

	public boolean send(Object message);
}
