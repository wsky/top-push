package com.tmall.top.push;

import com.tmall.top.push.messaging.EventMessage;

public class EventMessagePool extends Pool<EventMessage> {

	public EventMessagePool(int poolSize) {
		super(poolSize);
	}

	@Override
	public EventMessage createNew() {
		return new EventMessage();
	}

}
