package com.taobao.top.push.pulling;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PullingTriggers {
	protected static Logger logger = LoggerFactory.getLogger(PullingTriggers.class);

	private Timer dispatcher;
	private AtomicInteger counter = new AtomicInteger();
	private int maxTriggerCount = 50000;

	public PullingTriggers() {
		this.dispatcher = new Timer("trigger-dispacher", true);
	}

	public void setMaxTriggerCount(int value) {
		this.maxTriggerCount = value;
	}

	public boolean delayTrigger(final Object trigger, int delay) {
		return this.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					dispatch(trigger);
				} catch (Exception e) {
					logger.error("dispatch error", e);
				} finally {
					counter.decrementAndGet();
				}
			}
		}, delay);
	}

	protected abstract void dispatch(Object trigger);

	private boolean schedule(TimerTask task, int delay) {
		if (this.counter.get() >= this.maxTriggerCount)
			return false;

		this.dispatcher.schedule(task, delay);
		this.counter.incrementAndGet();
		return true;
	}
}
