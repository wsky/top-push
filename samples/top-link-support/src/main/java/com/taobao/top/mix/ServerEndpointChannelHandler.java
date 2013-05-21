package com.taobao.top.mix;

import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.endpoint.EndpointChannelHandler;

public class ServerEndpointChannelHandler extends EndpointChannelHandler {
	public ServerEndpointChannelHandler(LoggerFactory loggerFactory) {
		super(loggerFactory);
	}

	@Override
	public void onConnect(ChannelContext context) throws Exception {
		// TODO: base auth here for connection
	}
}