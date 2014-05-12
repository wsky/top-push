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

	private ExecutorService executor;
	private PullRequestPendings pendings;

	private int pullStep = 32;
	private int pullAmount = 320;
	private int pullMaxPendingCount = 1000;
	private int continuingTriggerDelay = 900;

	public PullRequestScheduler(PullRequestPendings pendings) {
		this.pendings = pendings;
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

		PullingState state = this.canPulling(client, request, amount);

		if (state == PullingState.FALSE)
			return;

		if (state == PullingState.Continuing) {
			this.continuingTrigger(request, this.continuingTriggerDelay);
			return;
		}

		try {
			if (!this.pendings.setPending(request))
				return;

			this.execute(new Runnable() {
				public void run() {
					try {
						pull(request, client, amount, new Callback() {
							private int pulled;
							private boolean isBreak;

							@Override
							public boolean onMessage(List<Object> messages, boolean ordering) {
								if (messages != null)
									pulled += messages.size();
								return this.isBreak = !sendMessages(client, messages, ordering);
							}

							@Override
							public void onComplete() {
								if (this.isBreak || this.pulled >= amount)
									continuingTrigger(request, continuingTriggerDelay);
							}
						});
					} catch (Exception e) {
						logger.error("pull error", e);
					} finally {
						// FIXME cancel here? how about pulling is async?
						pendings.cancelPending(request);
					}
				}
			});
		} catch (Exception e) {
			this.pendings.cancelPending(request);
			logger.error("dispatch error", e);
		}
	}

	protected boolean sendMessages(Client client, List<Object> messages, boolean ordering) {
		if (messages == null)
			return false;

		// TODO impl ordering push
		MessageSender sender = client.newSender();
		for (Object msg : messages)
			if (!this.isMessageSent(sender.send(msg)))
				return false;
		return true;
	}

	protected boolean isMessageSent(MessagingStatus status) {
		return status == MessagingStatus.SENT || status == MessagingStatus.ABORT;
	}

	protected void execute(Runnable task) {
		this.executor.submit(task);
	}

	protected PullingState canPulling(Client client, Object request, int amount) {
		if (amount <= 0)
			return PullingState.FALSE;

		if (this.isOffline(client))
			return PullingState.FALSE;

		if (this.reachPushMaxPending(client, request, amount))
			return PullingState.Continuing;

		return PullingState.TRUE;
	}

	protected boolean isOffline(Client client) {
		return client == null || client.getConnectionsCount() == 0;
	}

	protected boolean reachPushMaxPending(Client client, Object request, int amount) {
		return this.getPullMaxPendingCount(client, request) - client.getPendingMessagesCount() < (amount / 2);
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

	protected abstract void continuingTrigger(Object request, int delay);

	protected abstract void pull(Object request, Client client, int amount, Callback callback);

	interface Callback {
		public boolean onMessage(List<Object> messages, boolean ordering);

		public void onComplete();
	}

	enum PullingState {
		TRUE,
		FALSE,
		Continuing
	}
}
