package com.taobao.top.push.pulling;

import static junit.framework.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.taobao.top.push.Client;

public class PullRequestSchedulerTest {
	private static Object request = "pull_request";
	private static Client client = new Client("abc");

	@Test
	public void dispatch_test() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		PullingHandleMock handle = new PullingHandleMock() {
			@Override
			protected void pull(Object request, Client client, int amount, Callback callback) {
				latch.countDown();
			}
		};
		handle.dispatch(client, request);
		latch.await();
	}

	// pending flag

	@Test
	public void pending_test() throws InterruptedException {
		final PullRequestPendings pendings = new PullRequestPendings();
		PullingHandleMock handle = new PullingHandleMock(pendings) {
			@Override
			protected void pull(Object request, Client client, int amount, Callback callback) {
				assertFalse(pendings.setPending(request));
				callback.onComplete();
			}
		};
		handle.dispatch(client, request);
		assertTrue(pendings.setPending(request));
	}

	@Test
	public void pending_canceled_if_can_not_pulling_test() throws InterruptedException {
		final PullRequestPendings pendings = new PullRequestPendings();
		PullingHandleMock handle = new PullingHandleMock(pendings) {
			@Override
			protected PullingState canPulling(Client client, Object request, int amount) {
				return PullingState.FALSE;
			}
		};
		handle.dispatch(client, request);
		assertTrue(pendings.setPending(request));
	}

	@Test
	public void pending_canceled_if_pull_error_test() throws InterruptedException {
		final PullRequestPendings pendings = new PullRequestPendings();
		PullingHandleMock handle = new PullingHandleMock(pendings) {
			@Override
			protected void pull(Object request, Client client, int amount, Callback callback) {
				throw new NullPointerException();
			}
		};
		handle.dispatch(client, request);
		assertTrue(pendings.setPending(request));
	}

	@Test
	public void pending_canceled_if_execute_error_test() throws InterruptedException {
		final PullRequestPendings pendings = new PullRequestPendings();
		PullingHandleMock handle = new PullingHandleMock(pendings) {
			@Override
			protected void execute(Runnable task) {
				throw new NullPointerException();
			}
		};
		handle.dispatch(client, request);
		assertTrue(pendings.setPending(request));
	}

	// continuing trigger

	@Test
	public void continuing_test() {
		final AtomicInteger count = new AtomicInteger();
		PullingHandleMock handle = new PullingHandleMock() {
			@Override
			protected void pull(Object request, Client client, int amount, Callback callback) {
				callback.onMessage(null, false);
				callback.onComplete();
				count.incrementAndGet();
			}

			@Override
			protected void continuingTrigger(Object request, int delay) {
				count.incrementAndGet();
			}
		};
		handle.dispatch(client, request);
		assertEquals(2, count.get());
	}

	@Test
	public void still_continuing_if_can_not_pulling_as_reach_max_pending_test() {
		final AtomicInteger count = new AtomicInteger();
		PullingHandleMock handle = new PullingHandleMock() {
			@Override
			protected PullingState canPulling(Client client, Object request, int amount) {
				PullingState state = this.canPullingBase(client, request, 10);
				assertEquals(PullingState.Continuing, state);
				return state;
			}

			@Override
			protected void continuingTrigger(Object request, int delay) {
				count.incrementAndGet();
			}

			@Override
			protected boolean isOffline(Client client) {
				return false;
			}

			@Override
			protected boolean reachPushMaxPending(Client client, Object request, int amount) {
				return true;
			}
		};
		handle.dispatch(client, request);
		assertEquals(1, count.get());
	}

	class PullingHandleMock extends PullRequestScheduler {
		public PullingHandleMock() {
			this(new PullRequestPendings());
		}

		public PullingHandleMock(PullRequestPendings pendings) {
			this.setPendings(pendings);
		}

		@Override
		protected PullingState canPulling(Client client, Object request, int amount) {
			return PullingState.TRUE;
		}

		protected PullingState canPullingBase(Client client, Object request, int amount) {
			return super.canPulling(client, request, amount);
		}

		@Override
		protected void execute(Runnable task) {
			try {
				task.run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void continuingTrigger(Object request, int delay) {
		}

		@Override
		protected void pull(Object request, Client client, int amount, Callback callback) {
		}
	}
}