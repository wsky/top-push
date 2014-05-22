package com.taobao.top.push.pulling;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.top.push.Client;
import com.taobao.top.push.MessageSender;
import com.taobao.top.push.MessagingStatus;

public abstract class PullRequestScheduler {
	protected static Logger logger = LoggerFactory.getLogger(PullRequestScheduler.class);

	private PullRequestLocks locks = new PullRequestLocks();
	private ExecutorService executor;

	private int pullStep = 32;
	private int pullAmount = 320;
	private int pullMaxPendingCount = 1000;
	private int continuingTriggerDelay = 900;

	public void setLocks(PullRequestLocks locks) {
		this.locks = locks;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public void setPullMaxPendingCount(int value) {
		this.pullMaxPendingCount = value;
	}

	public void setPullAmount(int value) {
		this.pullAmount = value;
	}

	public void setPullStep(int value) {
		this.pullStep = value;
	}

	public void setContinuingTriggerDelayMillis(int value) {
		this.continuingTriggerDelay = value;
	}

	public void dispatch(final Client client, final Object request) {
		final int amount = this.getPullAmount(client, request);
		final int pullStep = this.getPullStep(client, request);

		PullingState state = this.acquirePulling(client, request, amount, pullStep);

		if (state == PullingState.FALSE ||
				state == PullingState.UNKNOWN ||
				state == PullingState.NO_VALID_CONNECTION ||
				state == PullingState.AMOUNT_ZERO ||
				state == PullingState.STEP_ZERO ||
				state == PullingState.LOCK)
			return;

		if (state == PullingState.MAX_PENDING ||
				state == PullingState.CONTINUE) {
			this.continuingTrigger(request, this.continuingTriggerDelay);
			return;
		}

		try {
			this.execute(new Runnable() {
				public void run() {
					try {
						final MessageSender sender = client.newSender();

						pull(request, client, amount, pullStep, new Callback() {
							private int pulled;
							private boolean isBreak;

							@Override
							public boolean onMessage(List<?> messages, boolean ordering) {
								if (messages != null)
									pulled += messages.size();
								// TODO impl send ordering
								return sendMessages(sender, client, request, messages) ? true : !(this.isBreak = true);
							}

							@Override
							public void onComplete() {
								releasePulling(client, request);

								PullingState state = afterPulling(this.isBreak, this.pulled, amount);

								if (state == PullingState.CONTINUE ||
										state == PullingState.BREAK)
									continuingTrigger(request, continuingTriggerDelay);
							}
						});
					} catch (Exception e) {
						releasePulling(client, request);
						logger.error("pull error", e);
					}
				}
			});
		} catch (Exception e) {
			releasePulling(client, request);
			logger.error("dispatch error", e);
		}
	}

	protected boolean sendMessages(MessageSender sender, Client client, Object request, List<?> messages) {
		if (messages == null)
			return false;

		boolean dropped = false;
		for (Object msg : messages) {
			if (dropped)
				this.dropMessage(client, request, msg);
			else if (!this.isMessageSent(sender.send(msg)))
				dropped = true;
		}
		return !dropped;
	}

	protected boolean isMessageSent(MessagingStatus status) {
		return status == MessagingStatus.SENT || status == MessagingStatus.ABORT;
	}

	protected void execute(Runnable task) {
		this.executor.submit(task);
	}

	protected PullingState acquirePulling(Client client, Object request, int amount, int pullStep) {
		PullingState state = this.canPulling(client, request, amount, pullStep);

		if (state != PullingState.TRUE)
			return state;

		if (!this.locks.acquire(client, request))
			return PullingState.LOCK;

		return PullingState.TRUE;
	}

	protected PullingState canPulling(Client client, Object request, int amount, int pullStep) {
		if (this.isInvalid(client))
			return PullingState.NO_VALID_CONNECTION;

		if (amount <= 0)
			return PullingState.AMOUNT_ZERO;

		if (pullStep <= 0)
			return PullingState.STEP_ZERO;

		if (this.reachPullMaxPending(client, request, amount))
			return PullingState.MAX_PENDING;

		return PullingState.TRUE;
	}

	protected void releasePulling(Client client, Object request) {
		this.locks.release(client, request);
	}

	protected PullingState afterPulling(boolean isBreak, int pulled, int amount) {
		if (isBreak)
			return PullingState.BREAK;

		if (pulled < amount)
			return PullingState.LESS_THAN_AMOUNT;

		return PullingState.CONTINUE;
	}

	protected boolean isInvalid(Client client) {
		return client == null || client.getValidConnectionCount() == 0;
	}

	protected boolean reachPullMaxPending(Client client, Object request, int amount) {
		return this.getPullMaxPendingCount(client, request) - client.getPendingMessageCount() < (amount / 2);
	}

	protected int getPullAmount(Client client, Object request) {
		return this.pullAmount;
	}

	protected int getPullStep(Client client, Object request) {
		return this.pullStep;
	}

	protected int getPullMaxPendingCount(Client client, Object request) {
		return this.pullMaxPendingCount;
	}

	protected void dropMessage(Client client, Object request, Object message) {
		// FIXME should store dropped messages
	}

	protected abstract void continuingTrigger(Object request, int delay);

	protected abstract void pull(Object request, Client client, int amount, int pullStep, Callback callback);

	public interface Callback {
		public boolean onMessage(List<?> messages, boolean ordering);

		public void onComplete();
	}
}
