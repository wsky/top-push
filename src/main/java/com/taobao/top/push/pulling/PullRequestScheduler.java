package com.taobao.top.push.pulling;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.top.push.Client;
import com.taobao.top.push.MessageSender;
import com.taobao.top.push.MessagingStatus;

public abstract class PullRequestScheduler {
	protected static Logger logger = LoggerFactory.getLogger(PullRequestScheduler.class);
	
	private ExecutorService executor;
	
	private int pullStep = 32;
	private int pullAmount = 320;
	private int pullMaxPendingCount = 1000;
	private int continuingTriggerDelay = 1000;
	private boolean fixedRate = true;
	
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
	
	public void setContinuingTriggerDelayInFixedRate(boolean value) {
		this.fixedRate = value;
	}
	
	public void dispatch(final Client client, final Object request, final AtomicBoolean continuing) {
		final int amount = this.getPullAmount(client, request);
		final int pullStep = this.getPullStep(client, request);
		
		PullingState state = this.canPulling(client, request, amount, pullStep);
		
		if (state == PullingState.FALSE ||
				state == PullingState.UNKNOWN ||
				state == PullingState.NO_VALID_CONNECTION ||
				state == PullingState.AMOUNT_ZERO ||
				state == PullingState.STEP_ZERO) {
			continuing.set(false);
			return;
		}
		
		if (state == PullingState.MAX_PENDING || state == PullingState.CONTINUE) {
			this.continuingTrigger(request, this.continuingTriggerDelay);
			return;
		}
		
		try {
			this.execute(new Runnable() {
				public void run() {
					try {
						final MessageSender sender = client.newSender();
						final long begin = System.currentTimeMillis();
						
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
								PullingState state = afterPulling(client, request, this.isBreak, this.pulled, amount);
								
								int cost = fixedRate ? (int) (System.currentTimeMillis() - begin) : 0;
								
								if (state == PullingState.CONTINUE || state == PullingState.BREAK)
									continuingTrigger(request, continuingTriggerDelay - cost);
								else
									continuing.set(false);
							}
						});
					} catch (Exception e) {
						continuing.set(false);
						logger.error("pull error", e);
					}
				}
			});
		} catch (Exception e) {
			continuing.set(false);
			logger.error("dispatch error", e);
		}
	}
	
	protected boolean sendMessages(MessageSender sender, Client client, Object request, List<?> messages) {
		if (messages == null)
			return false;
		
		boolean dropped = false;
		for (int i = 0; i < messages.size(); i++) {
			if (dropped) {
				this.dropMessage(client, request, messages.get(i));
				continue;
			}
			
			if (!this.isMessageSent(sender.send(messages.get(i)))) {
				dropped = true;
				i--;
			}
		}
		return !dropped;
	}
	
	protected boolean isMessageSent(MessagingStatus status) {
		return status == MessagingStatus.SENT ||
				status == MessagingStatus.ABORT ||
				status == MessagingStatus.IN_DOUBT;
	}
	
	protected void execute(Runnable task) {
		this.executor.submit(task);
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
	
	protected PullingState afterPulling(Client client, Object request, boolean isBreak, int pulled, int amount) {
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
