package com.tmall.top;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;

public class FrontendWebSocketServlet extends WebSocketServlet {

	private static Object _syncObject = new Object();
	private static Thread _workerThread;
	private static int _activePersent = 20;

	public static ConcurrentLinkedQueue<String> Messages = new ConcurrentLinkedQueue<String>();
	public static List<FrontendWebSocket> Clients = Collections
			.synchronizedList(new ArrayList<FrontendWebSocket>());

	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		if (_workerThread == null) {
			synchronized (_syncObject) {
				if (_workerThread == null) {
					String p = this.getInitParameter("activePersent");
					if (p != null && !p.isEmpty())
						_activePersent = Integer.parseInt(p);
					// start 4 forward-workers for backend
					(_workerThread = new Thread(new Forwarder())).start();
					(_workerThread = new Thread(new Forwarder())).start();
					(_workerThread = new Thread(new Forwarder())).start();
					(_workerThread = new Thread(new Forwarder())).start();

					// start 1 forward-workers for frontend
					// (_workerThread = new Thread(new Forwarder2())).start();
				}
				System.out.println(String.format(
						"worker running, activePersent=%s", _activePersent));
			}
		}

		return new FrontendWebSocket();
	}

	public class FrontendWebSocket implements OnTextMessage, WebSocket.OnFrame,
			WebSocket.OnControl {

		public Connection Connection;
		public FrameConnection FrameConnection;

		public void onClose(int arg0, String arg1) {
			synchronized (_syncObject) {
				FrontendWebSocketServlet.Clients.remove(this);
				System.out.println(String.format(
						"1 client closed, has connected %s clients",
						FrontendWebSocketServlet.Clients.size()));
			}
		}

		public void onOpen(Connection arg0) {
			this.Connection = arg0;
			FrontendWebSocketServlet.Clients.add(this);
			System.out.println(String.format("has connected %s clients",
					FrontendWebSocketServlet.Clients.size()));
		}

		public void onMessage(String arg0) {
			// Messages.add(arg0);
		}

		public boolean onControl(byte arg0, byte[] arg1, int arg2, int arg3) {
			// http://tools.ietf.org/html/rfc6455#section-5.5
			// http://www.kanasansoft.com/weblab/2012/07/send_ping_and_pong_frame_of_websocket_with_jetty.html
			// try {
			if (this.FrameConnection.isPing(arg0)) {
				/*
				 * if (this.FrameConnection.isPong((byte) 0x03)) {
				 * this.FrameConnection.sendControl((byte) 0x03, null, 0, 0); }
				 * else if (this.FrameConnection.isPong((byte) 0x0a)) {
				 * this.FrameConnection.sendControl((byte) 0x0a, null, 0, 0); }
				 */
				// System.out.println("receive ping");
			} else if (this.FrameConnection.isPong(arg0)) {
				/*
				 * if (this.FrameConnection.isPing((byte) 0x02)) {
				 * this.FrameConnection.sendControl((byte) 0x02, null, 0, 0); }
				 * else if (this.FrameConnection.isPing((byte) 0x09)) {
				 * this.FrameConnection.sendControl((byte) 0x09, null, 0, 0); }
				 */
				// System.out.println("receive pong");
			}
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			return false;
		}

		public boolean onFrame(byte arg0, byte arg1, byte[] arg2, int arg3,
				int arg4) {
			return false;
		}

		public void onHandshake(FrameConnection arg0) {
			this.FrameConnection = arg0;
		}
	}

	// read backend's forward to front-end
	private class Forwarder implements Runnable {
		private int _total;

		public void run() {

			while (true) {
				try {
					// HACK:use active percent, 20%/30%
					int size = (int) (FrontendWebSocketServlet.Clients.size()
							* _activePersent / 100);
					int count = 0;
					String msg;
					while ((msg = BackendWebSocketServlet.Messages.poll()) != null) {
						// TODO:set max send messages to prevent slow client?
						for (int i = 0; i < size; i++) {
							if (i >= FrontendWebSocketServlet.Clients.size())
								break;

							FrontendWebSocket client = FrontendWebSocketServlet.Clients
									.get(i);

							if (client != null && client.Connection != null)
							// && client.Connection.isOpen())//avoid some cost?
							{
								try {
									client.Connection.sendMessage(msg);
									count++;
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						this._total++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// read frontend's forward to backend
	private class Forwarder2 implements Runnable {

		public void run() {
			String msg;

			while (true) {
				while ((msg = FrontendWebSocketServlet.Messages.poll()) != null) {
					if (BackendWebSocketServlet.Backend != null
							&& BackendWebSocketServlet.Backend.isOpen())
						try {
							BackendWebSocketServlet.Backend.sendMessage(msg);
						} catch (IOException e) {
							e.printStackTrace();
						}
					else {
						FrontendWebSocketServlet.Messages.add(msg);
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
