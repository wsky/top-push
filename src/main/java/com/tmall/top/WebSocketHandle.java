package com.tmall.top;

import java.io.Console;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

public class WebSocketHandle extends WebSocketServlet {

	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		System.console().writer().println(arg1);
		return new ProxyWebSocket();
	}

	private class ProxyWebSocket implements WebSocket{
		private Outbound outbound;
		
		public void onConnect(Outbound arg0) {
			this.outbound = arg0;
		}

		public void onDisconnect() {

		}

		public void onFragment(boolean arg0, byte arg1, byte[] arg2, int arg3, int arg4) {
			
		}

		public void onMessage(byte arg0, String arg1) {
			try {
				this.outbound.sendMessage(arg1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void onMessage(byte arg0, byte[] arg1, int arg2, int arg3) {
					
		}
	}
}