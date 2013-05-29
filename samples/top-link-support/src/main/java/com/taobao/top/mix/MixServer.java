package com.taobao.top.mix;

import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.Logger;
import com.taobao.top.link.channel.ServerChannelSender;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;
import com.taobao.top.link.endpoint.Endpoint;
import com.taobao.top.link.endpoint.EndpointProxy;
import com.taobao.top.link.endpoint.SingleProxyStateHandler;
import com.taobao.top.link.schedule.Scheduler;
import com.taobao.top.push.PushManager;

// poll-push mix server
public class MixServer {
	private static Logger logger;
	private static PushManager pushManager;
	private static Endpoint serverEndpoint;
	private static Scheduler<com.taobao.top.link.endpoint.Identity> scheduler;

	public static void main(String[] args) {
		start(8080);
	}

	public static void start(int port) {
		// whatever, log first
		MixLoggerFactory loggerFactory = new MixLoggerFactory();
		logger = loggerFactory.create(MixServer.class);

		// for push support
		pushManager = new PushManager(loggerFactory, 50000,
				100,// care about senderCount by cpu usage
				1000);
		// pushManager.setClientStateHandler(null);
		// pushManager.setMessageStateHandler(messageStateHandler);
		logger.info("==== top-push init.");

		// init base server
		// scheduler for business process and loadbalance/attack-prevent
		scheduler = new FlowControl();
		scheduler.setUserMaxPendingCount(100);
		// biz-threadpool
		scheduler.setThreadPool(new ThreadPoolExecutor(20,
				200,// max threadpool size
				300, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
		scheduler.start();
		logger.info("==== scheduler(%s) start.", scheduler.getClass().getName());

		// extended low-level channelHandler
		ServerEndpointChannelHandler channelHandler = new ServerEndpointChannelHandler(loggerFactory);
		channelHandler.setScheduler(scheduler);
		channelHandler.setStateHandler(new SingleProxyStateHandler() {
			@Override
			public void onConnect(EndpointProxy endpoint, ServerChannelSender sender) throws LinkException {
				super.onConnect(endpoint, sender);
				// build pushClient here
				pushManager.connectClient(
						(ClientIdentity) endpoint.getIdentity(),
						new ClientEndpointConnection(endpoint, sender));
			}
		});
		// your server for talking
		serverEndpoint = new Endpoint(loggerFactory, new ServerIdentity());
		serverEndpoint.setChannelHandler(channelHandler);
		serverEndpoint.setMessageHandler(new ServerMessageHandler());
		// websocket serverchannel
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(loggerFactory, port, true);
		// if you want SSL/TLS
		// serverChannel.setSSLContext(sslContext);
		serverEndpoint.bind(serverChannel);
		logger.info("==== mix-server bind at port %s", port);
	}

	public static void stop() throws InterruptedException {
		pushManager.cancelAll();
		scheduler.stop();
		serverEndpoint.unbindAll();
		logger.info("==== mix-server stop");
	}

	public static void pending(ClientIdentity target, HashMap<String, String> message) {
		pushManager.getClient(target).pendingMessage(message);
	}

	public static void disconnect(ClientIdentity id, String reasonText) {
		pushManager.disconnectClient(id, reasonText);
	}
}
