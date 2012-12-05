package com.tmall.top;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;

import com.tmall.top.FrontendWebSocketServlet.FrontendWebSocket;

public class BackendWebSocketServlet extends WebSocketServlet {

	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		return new BackendWebSocket();
	}
	
	private class BackendWebSocket implements OnTextMessage {
		
		private int _total = 0;
		
		public void onClose(int arg0, String arg1) {}

		public void onOpen(Connection arg0) {}

		public void onMessage(String arg0) {
			_total++;
			
			int size = FrontendWebSocketServlet.Clients.size();
			int count = 0;
			for(int i = 0; i < size; i++) {
				if(i >= FrontendWebSocketServlet.Clients.size())
					break;
				
				FrontendWebSocket client = FrontendWebSocketServlet.Clients.get(i);
				if(client.Connection.isOpen())
					try {
						client.Connection.sendMessage(arg0);
						count++;
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			System.out.println(String.format("has send %s messages to %s clients at %s", this._total, count, new Date()));
		}
		
	}

}
