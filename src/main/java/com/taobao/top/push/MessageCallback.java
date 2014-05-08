package com.taobao.top.push;

public interface MessageCallback {

	public void onSent(Client client, Object message);

	public void onTimeout(Client client, Object message);

	public void onFault(Client client, Object message);
}
