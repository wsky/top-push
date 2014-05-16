package com.taobao.top.push.pulling;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

import com.taobao.top.push.Client;

public class PullRequestSchedulerTest {
	private static Object request = "pull_request";
	private static Client client = new Client("abc");

	@After
	public void after() {
		new PullRequestLocks().release(client);
	}

	@Test
	public void dispatch_test() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		PullingHandleMock handle = new PullingHandleMock() {
			@Override
			protected void pull(Object request, Client client, int amount, int pullStep, Callback callback) {
				callback.onComplete();
				latch.countDown();
			}
		};
		handle.dispatch(client, request);
		latch.await();
	}

	// pending flag

	@Test
	public void lock_test() throws InterruptedException {
		final PullRequestLocks locks = new PullRequestLocks();
		PullingHandleMock handle = new PullingHandleMock(locks) {
			@Override
			protected void pull(Object request, Client client, int amount, int pullStep, Callback callback) {
				assertFalse(locks.acquire(client, request));
				callback.onComplete();
			}
		};
		handle.dispatch(client, request);
		assertTrue(locks.acquire(client, request));
	}

	@Test
	public void lock_released_if_can_not_pulling_test() throws InterruptedException {
		final PullRequestLocks locks = new PullRequestLocks();
		PullingHandleMock handle = new PullingHandleMock(locks) {
			@Override
			protected PullingState canPulling(Client client, Object request, int amount, int pullStep) {
				return PullingState.FALSE;
			}
		};
		handle.dispatch(client, request);
		assertTrue(locks.acquire(client, request));
	}

	@Test
	public void lock_released_if_pull_error_test() throws InterruptedException {
		final PullRequestLocks locks = new PullRequestLocks();
		PullingHandleMock handle = new PullingHandleMock(locks) {
			@Override
			protected void pull(Object request, Client client, int amount, int pullStep, Callback callback) {
				throw new NullPointerException();
			}
		};
		handle.dispatch(client, request);
		assertTrue(locks.acquire(client, request));
	}

	@Test
	public void lock_released_if_execute_error_test() throws InterruptedException {
		final PullRequestLocks locks = new PullRequestLocks();
		PullingHandleMock handle = new PullingHandleMock(locks) {
			@Override
			protected void execute(Runnable task) {
				throw new NullPointerException();
			}
		};
		handle.dispatch(client, request);
		assertTrue(locks.acquire(client, request));
	}

	// continuing trigger

	@Test
	public void continuing_test() {
		final AtomicInteger count = new AtomicInteger();
		PullingHandleMock handle = new PullingHandleMock() {
			@Override
			protected void pull(Object request, Client client, int amount, int pullStep, Callback callback) {
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
			protected PullingState canPulling(Client client, Object request, int amount, int pullStep) {
				PullingState state = this.canPullingBase(client, request, 10, 1);
				assertEquals(PullingState.MAX_PENDING, state);
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

	@Test
	public void drop_test() {
		final AtomicInteger count = new AtomicInteger();
		PullingHandleMock mock = new PullingHandleMock() {
			@Override
			protected void dropMessage(Client client, Object request2, Object message) {
				assertEquals(request, request2);
				count.incrementAndGet();
			}

			@Override
			protected void pull(Object request, Client client, int amount, int pullStep, Callback callback) {
				List<Object> messages = new ArrayList<Object>();
				messages.add(0);
				messages.add(1);
				messages.add(2);
				assertFalse(callback.onMessage(messages, false));
			}
		};
		mock.dispatch(client, request);
		assertEquals(2, count.get());
	}

	class PullingHandleMock extends PullRequestScheduler {
		public PullingHandleMock() {
			this(new PullRequestLocks());
		}

		public PullingHandleMock(PullRequestLocks locks) {
			this.setLocks(locks);
		}

		@Override
		protected PullingState canPulling(Client client, Object request, int amount, int pullStep) {
			return PullingState.TRUE;
		}

		protected PullingState canPullingBase(Client client, Object request, int amount, int pullStep) {
			return super.canPulling(client, request, amount, pullStep);
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
		protected void pull(Object request, Client client, int amount, int pullStep, Callback callback) {
		}
	}
}