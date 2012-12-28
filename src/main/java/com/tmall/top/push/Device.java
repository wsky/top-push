package com.tmall.top.push;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.tmall.top.push.messages.Message;

public interface Device {
	public void Store(Message message);

	public void Delete(Message message);

	public class Client {
		//sub
		HashMap<String, ConcurrentLinkedQueue<Message>> pendingMessages;
	}
}
