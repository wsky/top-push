package com.taobao.top.mix;

import java.util.HashMap;

import com.taobao.top.link.endpoint.EndpointContext;
import com.taobao.top.link.endpoint.MessageHandler;

public class ServerMessageHandler implements MessageHandler {
	@Override
	public void onMessage(HashMap<String, String> message) {
	}

	@Override
	public void onMessage(EndpointContext context) throws Exception {
		// process client call here
		context.reply(context.getMessage());
	}
}
