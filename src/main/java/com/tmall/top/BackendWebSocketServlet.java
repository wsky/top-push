package com.tmall.top;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;

import com.tmall.top.FrontendWebSocketServlet.FrontendWebSocket;

public class BackendWebSocketServlet extends WebSocketServlet {

	public static ConcurrentLinkedQueue<String> Messages = new ConcurrentLinkedQueue<String>();
	public static Connection Backend = null;

	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		return new BackendWebSocket();
	}

	private class BackendWebSocket implements OnTextMessage {

		private int _total;

		public void onClose(int arg0, String arg1) {
		}

		public void onOpen(Connection arg0) {
			Backend = arg0;
		}

		public void onMessage(String arg0) {

			if (this._total == 0) {
				this._total = Integer.parseInt(arg0);
				return;
			}
			for (int i = 0; i < this._total; i++)
				Messages.add(arg0);
		}

	}

}
