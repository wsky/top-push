package com.tmall.top;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

//send and receive all
public class SimpleClientTest {
	public static String msg = "";
	public static int total;
	public static int size = 1024 * 64;

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < size; i++)
			msg += "i";

		// args
		String uri = "ws://localhost:8080/simple";
		total = 10 * 10000;
		int count_connect = 20000;

		WebSocketClientFactory factory = new WebSocketClientFactory();
		factory.setBufferSize(1024 * 1024);
		factory.start();

		for (int i = 0; i < count_connect; i++)
			Connect(factory, uri);
	}

	static void Connect(WebSocketClientFactory factory, String uri)
			throws InterruptedException, ExecutionException, TimeoutException,
			IOException, URISyntaxException {
		WebSocketClient client = factory.newWebSocketClient();
		client.setMaxTextMessageSize(size * 10);
		WebSocket.Connection connection = client.open(new URI(uri),
				new WebSocket.OnTextMessage() {
					int count_received = 0;
					int count_match = 0;

					public void onOpen(Connection connection) {
					}

					public void onClose(int closeCode, String message) {
					}

					public void onMessage(String data) {
						count_received++;
						if (data.length() == msg.length())
							count_match++;
						if (count_received == total)
							System.out.println(String.format(
									"received:%s, matchs:%s", count_received,
									count_match));
					}
				}).get(1, TimeUnit.SECONDS);

		connection.setMaxTextMessageSize(size * 10);
		connection.sendMessage(total + "");
		connection.sendMessage(msg);
	}
}
