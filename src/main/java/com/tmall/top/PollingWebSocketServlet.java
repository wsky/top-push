package com.tmall.top;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;

import sun.misc.Lock;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.tools.javac.util.List;

/*
 * keep client connections for polling and send messages
 */
public class PollingWebSocketServlet extends WebSocketServlet {
	
	private static Object _syncObject = new Object();
	private static Thread _workerThread;
	private static java.util.List<PollingWebSocket> _clients = Collections.synchronizedList(new ArrayList<PollingWebSocket>());
	
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		if(_workerThread!=null) {
			synchronized(_syncObject){
				if(_workerThread!=null)
					_workerThread =	new Thread(new PollingSender());
			}
		}
		return new PollingWebSocket();
	}

	public class PollingWebSocket implements OnTextMessage {
		public Connection Connection;
		public int Total, SendCount;
		public String Message;
		
		public void onClose(int arg0, String arg1) { 
			PollingWebSocketServlet._clients.remove(this);
		}
		
		public void onOpen(Connection arg0) {
			this.Connection = arg0;
			PollingWebSocketServlet._clients.add(this);
		}
		
		public void onMessage(String arg0) {
			if(this.Total == 0) {
				this.Total = Integer.parseInt(arg0);
				return;
			}
			this.Message = arg0;
		}
	}
	private class PollingSender implements Runnable {

		public void run() {
			while(true) {
				for(int i = 0; i < PollingWebSocketServlet._clients.size(); i++) {
					PollingWebSocket client = PollingWebSocketServlet._clients.get(i);
					
					if(!client.Connection.isOpen() || client.Total == client.SendCount) 
						continue;
					
					for(int j = 0; j < client.Total; j++){
						try {
							client.Connection.sendMessage(client.Message);
							client.SendCount++;
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
