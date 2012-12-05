package com.tmall.top;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;

public class FrontendWebSocketServlet extends WebSocketServlet {
	
	private static Object _syncObject = new Object();
	private static Thread _workerThread;
	
	public static List<FrontendWebSocket> Clients = Collections.synchronizedList(new ArrayList<FrontendWebSocket>()); 
	
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		/*if(_workerThread == null) {
			synchronized(_syncObject){
				if(_workerThread == null) {
					_workerThread =	new Thread(new Forwarder());
					_workerThread.start();
					System.out.println("worker running...");
				}
			}
		}*/
		return new FrontendWebSocket();
	}
	
	public class FrontendWebSocket implements OnTextMessage {
		
		public Connection Connection;
		
		public void onClose(int arg0, String arg1) {
			synchronized (_syncObject) {
				FrontendWebSocketServlet.Clients.remove(this);
				System.out.println(String.format("1 client closed, has connected %s clients", FrontendWebSocketServlet.Clients.size()));
			}
		}

		public void onOpen(Connection arg0) {
			this.Connection = arg0;
			FrontendWebSocketServlet.Clients.add(this);
			System.out.println(String.format("has connected %s clients", FrontendWebSocketServlet.Clients.size()));
		}

		public void onMessage(String arg0) {}
		
	}
	
	private class Forwarder implements Runnable {

		public void run() {
			
		}
		
	}
}
