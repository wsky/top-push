package com.taobao.top.push.pulling;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.taobao.top.push.Client;
import com.taobao.top.push.MessagingStatus;
import com.taobao.top.push.pulling.PullRequestLocks.Lock;

public class PullRequestSchedulerTest {
	private static Object request = "pull_request";
	private static Client client = new Client("abc");
	private static Lock continuing = new Lock();
	private static int timeout = 10;
	
	@Before
	public void before() {
		continuing.lock();
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
		handle.dispatch(client, request, continuing);
		latch.await();
	}
	
	@Test
	public void continuing_false_if_less_than_amount_test() throws InterruptedException {
		PullingHandleMock handle = new PullingHandleMock() {
			@Override
			protected void pull(Object request, Client client, int amount, int pullStep, Callback callback) {
				// no messagaes
				callback.onComplete();
			}
			
			@Override
			protected PullingState afterPulling(Client client, Object request, boolean isBreak, int pulled, int amount) {
				PullingState state = super.afterPulling(client, request, isBreak, pulled, amount);
				assertEquals(PullingState.LESS_THAN_AMOUNT, state);
				return state;
				
			}
		};
		handle.dispatch(client, request, continuing);
		assertFalse(continuing.isLocked(timeout));
	}
	
	@Test
	public void continuing_false_if_can_not_pulling_test() throws InterruptedException {
		PullingHandleMock handle = new PullingHandleMock() {
			@Override
			protected PullingState canPulling(Client client, Object request, int amount, int pullStep) {
				return PullingState.FALSE;
			}
		};
		handle.dispatch(client, request, continuing);
		assertFalse(continuing.isLocked(timeout));
	}
	
	@Test
	public void continuing_false_if_pull_error_test() throws InterruptedException {
		PullingHandleMock handle = new PullingHandleMock() {
			@Override
			protected void pull(Object request, Client client, int amount, int pullStep, Callback callback) {
				throw new NullPointerException();
			}
		};
		handle.dispatch(client, request, continuing);
		assertFalse(continuing.isLocked(timeout));
	}
	
	@Test
	public void continuing_false_if_execute_error_test() throws InterruptedException {
		PullingHandleMock handle = new PullingHandleMock() {
			@Override
			protected void execute(Runnable task) {
				throw new NullPointerException();
			}
		};
		handle.dispatch(client, request, continuing);
		assertFalse(continuing.isLocked(timeout));
	}
	
	@Test
	public void continuing_trigger_test() {
		final AtomicInteger count = new AtomicInteger();
		PullingHandleMock handle = new PullingHandleMock() {
			@Override
			protected void pull(Object request, Client client, int amount, int pullStep, Callback callback) {
				callback.onMessage(null, false);
				callback.onComplete();
				count.incrementAndGet();
			}
			
			@Override
			protected void continuingTrigger(Client client, Object request, Lock continuing, int delay) {
				count.incrementAndGet();
			}
		};
		handle.dispatch(client, request, continuing);
		assertEquals(2, count.get());
		assertTrue(continuing.isLocked(timeout));
	}
	
	@Test
	public void still_continuing_trigger_if_can_not_pulling_as_reach_max_pending_test() {
		final AtomicInteger count = new AtomicInteger();
		PullingHandleMock handle = new PullingHandleMock() {
			@Override
			protected PullingState canPulling(Client client, Object request, int amount, int pullStep) {
				PullingState state = this.canPullingBase(client, request, 10, 1);
				assertEquals(PullingState.MAX_PENDING, state);
				return state;
			}
			
			@Override
			protected void continuingTrigger(Client client, Object request, Lock continuing, int delay) {
				count.incrementAndGet();
			}
			
			@Override
			protected boolean isInvalid(Client client) {
				return false;
			}
			
			@Override
			protected boolean reachPullMaxPending(Client client, Object request, int amount) {
				return true;
			}
		};
		handle.dispatch(client, request, continuing);
		assertEquals(1, count.get());
		assertTrue(continuing.isLocked(timeout));
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
			
			@Override
			protected boolean isMessageSent(MessagingStatus status) {
				assertEquals(MessagingStatus.NONE_CONNECTION, status);
				return super.isMessageSent(status);
			}
		};
		mock.dispatch(client, request, continuing);
		assertEquals(3, count.get());
	}
	
	class PullingHandleMock extends PullRequestScheduler {
		public PullingHandleMock() {
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
		protected void continuingTrigger(Client client, Object request, Lock continuing, int delay) {
		}
		
		@Override
		protected void pull(Object request, Client client, int amount, int pullStep, Callback callback) {
		}
	}
}