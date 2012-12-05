package com.tmall.top;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;

import sun.misc.Cleaner;
import sun.misc.Lock;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.tools.javac.util.List;

/*
 * keep client connections for polling and send messages
 */
public class PollingWebSocketServlet extends WebSocketServlet {
	
	private static Object _syncObject = new Object();
	private static Thread _workerThread;
	//arraylist not threadsafe, evne lose element!
	private static java.util.List<PollingWebSocket> _clients =Collections.synchronizedList(new ArrayList<PollingWebSocket>());
	
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		if(_workerThread == null) {
			synchronized(_syncObject){
				if(_workerThread == null) {
					_workerThread =	new Thread(new PollingSender());
					_workerThread.start();
					System.out.println("worker running...");
				}
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
			System.out.println("client closed");
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
			System.out.println(String.format("[polling] #%s total=%s, message=%s", this.hashCode(), this.Total, this.Message));
		}
	}
	private class PollingSender implements Runnable {

		public void run() {
			while(true) {
				int size = PollingWebSocketServlet._clients.size();
				//System.out.print("size:"+size);
				for(int i = 0; i < size; i++) {
					//System.out.print(i);
					if(i >= PollingWebSocketServlet._clients.size())
						break;
					
					PollingWebSocket client = PollingWebSocketServlet._clients.get(i);
					
					if(!client.Connection.isOpen() || 
							client.Total == client.SendCount || 
							client.Total == 0 || 
							client.Message == null) {
						/*System.out.println(String.format("client is closed or finished or not ready, %s %s %s", 
								client.Total, 
								client.SendCount, 
								client.Message));*/
						continue;
					}
					
					for(int j = 0; j < client.Total; j++){
						try {
							client.Connection.sendMessage(client.Message);
							client.SendCount++;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					System.out.println(String.format("[Polling] Send %s meesages", client.SendCount));
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
