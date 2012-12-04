package com.tmall.top;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;

/*
 * directky send counted messages in websocket handler
 */
public class SimpleWebSocketServlet extends WebSocketServlet {

	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		return new SimpleWebSocket();
	}

	private class SimpleWebSocket implements OnTextMessage{

		private Connection _connection;
		private int _total = 0;
		
		public void onClose(int arg0, String arg1) {}

		public void onOpen(Connection arg0) { this._connection = arg0; }

		public void onMessage(String msg) {
			if(this._total == 0) {
				this._total = Integer.parseInt(msg);
				return;
			}
		
			try {				
				for(int i = 0; i < this._total; i++) {
					this._connection.sendMessage(msg);
				}
				System.out.println(String.format("Send %s meesages", this._total));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
}