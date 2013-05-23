package com.taobao.top.mix;

import java.net.URI;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannelSharedSelector;
import com.taobao.top.link.endpoint.Endpoint;
import com.taobao.top.link.endpoint.EndpointContext;
import com.taobao.top.link.endpoint.EndpointProxy;
import com.taobao.top.link.endpoint.MessageHandler;

// poll-push mix client
public class MixClient {
	private Logger logger;
	private ClientIdentity id;
	private Endpoint endpoint;
	private MixClientHandler handler;
	private URI serverUri;
	private EndpointProxy server;

	private Timer timer;
	protected int reconnectInterval = 30000;

	public MixClient(ClientIdentity id) {
		// whatever, log first
		LoggerFactory loggerFactory = new MixLoggerFactory();
		this.logger = loggerFactory.create(this);

		// sharedpool with heartbeat 60s
		ClientChannelSharedSelector selector = new ClientChannelSharedSelector(loggerFactory);
		selector.setHeartbeat(60000);

		this.endpoint = new Endpoint(loggerFactory, this.id = id);
		this.endpoint.setClientChannelSelector(selector);
		this.endpoint.setMessageHandler(this.createHandler());

		this.logger.info("mix-client#%s init", this.id);
	}

	public void setClientHandler(MixClientHandler handler) {
		this.handler = handler;
	}

	public void connect(URI uri) throws LinkException {
		// extra header for validate auth
		HashMap<String, String> headers = new HashMap<String, String>();
		this.server = this.endpoint.getEndpoint(new ServerIdentity(), uri, headers);
		this.serverUri = uri;
		this.doReconnect();
		this.logger.info("connect to mix-server: %s", this.serverUri);
	}

	// TODO:define your request
	public void poll() throws ChannelException {
		HashMap<String, String> msg = new HashMap<String, String>();
		this.server.send(msg);
	}

	public void confirm() throws LinkException {
		HashMap<String, String> msg = new HashMap<String, String>();
		this.server.sendAndWait(msg, 1000);
	}

	private MessageHandler createHandler() {
		return new MessageHandler() {
			@Override
			public void onMessage(HashMap<String, String> message) {
				logger.info("got poll message:" + message);
				if (handler != null)
					handler.onPollResult(new MixMessage[0]);
			}

			@Override
			public void onMessage(EndpointContext context) throws LinkException {
				logger.info("got push message:" + context.getMessage());
				if (handler != null) {
					try {
						handler.onPushMessage(parseMessage(context.getMessage()));
					} catch (Exception e) {
						return;
					}
				}
				// then confirm if none exception
				HashMap<String, String> msg = new HashMap<String, String>();
				msg.put("confirm", "123");
				context.reply(msg);
			}
		};
	}

	private void doReconnect() {
		this.stopReconnect();
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					if (server.hasValidSender())
						return;
				} catch (Exception e) {
					logger.warn(e);
				}
				try {
					logger.warn("reconnecting...");
					connect(serverUri);
				} catch (LinkException e) {
					logger.warn("reconnect error", e);
				}
			}
		}, this.reconnectInterval, this.reconnectInterval);
	}

	private void stopReconnect() {
		if (this.timer == null)
			return;
		this.timer.cancel();
	}

	private MixMessage parseMessage(HashMap<String, String> raw) {
		return new MixMessage();
	}

	public interface MixClientHandler {
		// raise after poll
		public void onPollResult(MixMessage[] messages);

		// raise by server push
		public void onPushMessage(MixMessage message) throws Exception;
	}

	public class MixMessage {

	}
}
