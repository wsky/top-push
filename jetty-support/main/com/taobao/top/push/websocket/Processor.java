package com.taobao.top.push.websocket;

import com.taobao.top.push.ClientConnection;
import com.taobao.top.push.messages.Message;

// deal with command message like CONNECT/DISCONNECT
public class Processor {
	public boolean process(Message message, ClientConnection connection)
			throws Exception {
		return false;
	}
}