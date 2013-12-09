package com.taobao.top.push;

import java.util.concurrent.atomic.AtomicInteger;

public class DeliveryRater {
	private int deliveryRatePeriod;
	private float lastDeliveryRate;
	private long lastPeriodBegin;
	private AtomicInteger sendCountInPeriod;
	private AtomicInteger deliveryNumberInPeriod;

	public DeliveryRater() {
		this.sendCountInPeriod = new AtomicInteger(0);
		this.deliveryNumberInPeriod = new AtomicInteger(0);
	}

	public void setDeliveryRatePeriodMillis(int value) {
		this.deliveryRatePeriod = value;
	}

	public void increaseSendCount(int value) {
		this.sendCountInPeriod.addAndGet(value);
	}

	public void increaseDeliveryNubmer(int value) {
		this.deliveryNumberInPeriod.addAndGet(value);
	}

	public synchronized float getDeliveryRate() {
		long current = System.currentTimeMillis();
		if (current - this.lastPeriodBegin <= this.deliveryRatePeriod)
			return this.lastDeliveryRate;
		this.lastDeliveryRate = this.sendCountInPeriod.floatValue() > 0 ?
				this.deliveryNumberInPeriod.floatValue() / this.sendCountInPeriod.floatValue() : 1;
		this.lastPeriodBegin = current;
		this.sendCountInPeriod.set(0);
		this.deliveryNumberInPeriod.set(0);
		return this.lastDeliveryRate;
	}
}
