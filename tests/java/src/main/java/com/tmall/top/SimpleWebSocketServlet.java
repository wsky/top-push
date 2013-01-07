package com.tmall.top;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;

/*
 * directky send counted messages in websocket handler
 */
public class SimpleWebSocketServlet extends WebSocketServlet {
	
	private static Object lockObject = new Object();
	
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		return new SimpleWebSocket();
	}

	private class SimpleWebSocket implements OnTextMessage{

		private Connection _connection;
		private int _total = 0;
		
		public void onClose(int arg0, String arg1) {}

		public void onOpen(Connection arg0) { 
			this._connection = arg0;
		}

		public void onMessage(String msg) {
			if(this._total == 0) {
				this._total = Integer.parseInt(msg);
				return;
			}
			/*
			int worker = 4;
			int per = this._total / worker;
			
			for(int i = 0; i < worker; i++) {
				new Thread(new Sender(this, per, msg)).start();
			}
			
			return;*/
			long begin = System.currentTimeMillis();
			try {				
				for(int i = 0; i < this._total; i++) {
					this._connection.sendMessage(msg);
				}
				System.out.println(String.format("[Simple] Send %s meesages in %sms", this._total, System.currentTimeMillis() - begin));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class SimpleBinaryWebSocket implements OnBinaryMessage {
		
		private Connection _connection;
		private int _total = 0;
		
		public void onClose(int arg0, String arg1) {
			
		}

		public void onOpen(Connection arg0) { this._connection = arg0; }

		public void onMessage(byte[] arg0, int arg1, int arg2) {
		
		}
		
	}
	
	private class Sender implements Runnable {
		private SimpleWebSocket _client;
		private int _count;
		private String _message;
		public Sender(SimpleWebSocket client, int count, String message) {
			this._client = client;
			this._count = count;
			this._message = message;
		}
		
		public void run() {
			int success = 0;
			for (int i = 0; i < _count; i++) {
				try {
					this._client._connection.sendMessage(this._message);
					success++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println(String.format("Send %s messages in curren worker", success));
		}
		
	}
}