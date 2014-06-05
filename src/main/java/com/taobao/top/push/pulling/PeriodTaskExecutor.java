package com.taobao.top.push.pulling;

import java.util.Timer;
import java.util.TimerTask;

public abstract class PeriodTaskExecutor {
	private int period;
	private Timer timer;
	private TimerTask task;

	public int getPeriod() {
		return this.period;
	}
	
	public void setPeriod(int period) {
		this.period = period;

		if (this.timer == null)
			this.timer = new Timer(this.getExecutorName(), true);

		if (this.task != null)
			this.task.cancel();

		this.timer.purge();
		this.timer.schedule(this.task = this.createTask(), period, period);
	}

	protected String getExecutorName() {
		return this.getClass().getSimpleName();
	}

	protected abstract TimerTask createTask();
}
