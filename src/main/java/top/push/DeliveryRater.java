package top.push;

import java.util.concurrent.atomic.AtomicLong;

public class DeliveryRater {
	private int deliveryRatePeriod = 5000;
	private long latestPeriodBegin;
	private AtomicLong sendCountInPeriod;
	private AtomicLong deliveryNumberInPeriod;

	private float delta = 0.11F;
	private float recoveryStep = 0.1F;

	private float latestRate;
	private boolean recoveryExpired;
	private float recoveryRate = 1;

	public DeliveryRater() {
		this.sendCountInPeriod = new AtomicLong(0);
		this.deliveryNumberInPeriod = new AtomicLong(0);
	}

	public void setDeliveryRatePeriodMillis(int value) {
		this.deliveryRatePeriod = value;
	}

	public void setDelta(float value) {
		this.delta = value;
	}

	public void setRecoveryStep(float value) {
		this.recoveryStep = value;
	}

	public void increaseSendCount(int value) {
		this.sendCountInPeriod.addAndGet(value);
	}

	public void increaseDeliveryNubmer(int value) {
		this.deliveryNumberInPeriod.addAndGet(value);
	}

	public synchronized float getLatestDeliveryRate() {
		if (!this.isNextPeriod())
			return this.latestRate;

		this.latestRate = this.adapt(this.sendCountInPeriod.floatValue() > 0 ?
				this.deliveryNumberInPeriod.floatValue() / this.sendCountInPeriod.floatValue() : 1);

		this.sendCountInPeriod.set(0);
		this.deliveryNumberInPeriod.set(0);
		return this.latestRate;
	}

	public synchronized float calculateSmartRate() {
		float latest = this.getLatestDeliveryRate();

		if (!this.recoveryExpired)
			return this.recoveryRate;
		this.recoveryExpired = false;

		return this.recoveryRate = latest < 1 ?
				this.recoveryRate * latest :
				this.parse(this.recoveryRate + this.recoveryStep);
	}

	private boolean isNextPeriod() {
		long current = System.currentTimeMillis();
		if (current - this.latestPeriodBegin < this.deliveryRatePeriod)
			return false;
		this.latestPeriodBegin = current;
		// maybe if sendCountInPeriod==0, keep rate?
		this.recoveryExpired = true;
		return true;
	}

	private float adapt(float value) {
		return this.parse(1 - value <= this.delta ? 1F : value);
	}

	private float parse(float value) {
		if (value < 0)
			return 0F;
		if (value > 1)
			return 1F;
		return value;
	}
}
