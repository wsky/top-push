package com.tmall.top.push;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.tmall.top.push.messaging.ConfirmMessage;
import com.tmall.top.push.messaging.EventMessage;

public class Receiver {
	private EventMessagePool eventMessagePool;
	private ConfirmMessagePool confirmMessagePool;

	// TODO:choose a ring-buffer instead
	public ConcurrentLinkedQueue<ConfirmMessage> confirms;
	public ConcurrentLinkedQueue<EventMessage> events;

	public Receiver(int eventMessagePoolSize, int confirmMessagePoolSize) {
		this.eventMessagePool = new EventMessagePool(eventMessagePoolSize);
		this.confirmMessagePool = new ConfirmMessagePool(confirmMessagePoolSize);

		this.confirms = new ConcurrentLinkedQueue<ConfirmMessage>();
		this.events = new ConcurrentLinkedQueue<EventMessage>();
	}
	
	public EventMessage acquireEventMessage() {
		return this.eventMessagePool.acquire();
	}
	public ConfirmMessage acquireConfirmMessage() {
		return this.confirmMessagePool.acquire();
	}
	
	public void receive(ConfirmMessage msg) {
		this.confirms.add(msg);
	}
	public void receive(EventMessage msg) {
		this.events.add(msg);
	}
}
