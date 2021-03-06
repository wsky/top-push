package top.push.pulling;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

import top.push.Client;
import top.push.pulling.PullRequestLocks;
import top.push.pulling.PullRequestLocks.Lock;

public class PullRequestLocksTest {
	private Client client = new Client(1);
	
	@After
	public void after() {
		new PullRequestLocks().release(client);
	}
	
	@Test
	public void acquire_test() {
		PullRequestLocks locks = new PullRequestLocks();
		
		assertTrue(locks.acquire(client, 1));
		assertFalse(locks.acquire(client, 1));
		
		locks.release(client, 1);
		assertTrue(locks.acquire(client, 1));
		assertNull(locks.acquireAndGet(client, 1));
		
		locks.release(client, 1);
		Lock lock = locks.acquireAndGet(client, 1);
		assertTrue(lock.isLocked(100));
		assertNull(locks.acquireAndGet(client, 1));
		lock.unlock();
		assertNotNull(locks.acquireAndGet(client, 1));
		lock.lock();
		assertNull(locks.acquireAndGet(client, 1));
	}
	
	@Test
	public void release_test() {
		PullRequestLocks locks = new PullRequestLocks();
		
		assertTrue(locks.acquire(client, 1));
		assertFalse(locks.acquire(client, 1));
		
		assertTrue(locks.acquire(client, 2));
		assertFalse(locks.acquire(client, 2));
		
		locks.release(client);
		assertTrue(locks.acquire(client, 1));
		assertTrue(locks.acquire(client, 2));
	}
	
	@Test
	public void timeout_test() throws InterruptedException {
		PullRequestLocks locks = new PullRequestLocks();
		locks.setTimeout(100);
		assertTrue(locks.acquire(client, 1));
		assertFalse(locks.acquire(client, 1));
		Thread.sleep(500);
		assertTrue(locks.acquire(client, 1));
		
		locks.release(client);
		Lock lock = locks.acquireAndGet(client, 1);
		assertTrue(lock.isLocked(10));
		Thread.sleep(20);
		assertFalse(lock.isLocked(10));
		lock.lock();
		assertTrue(lock.isLocked(10));
	}
	
	@Test
	public void threaded_test() throws InterruptedException {
		int total = 10;
		final CountDownLatch latch = new CountDownLatch(total);
		final AtomicInteger count = new AtomicInteger();
		final PullRequestLocks locks = new PullRequestLocks();
		for (int i = 0; i < total; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (locks.acquire(client, 1))
						count.incrementAndGet();
					latch.countDown();
				}
			}).start();
		}
		latch.await();
		assertEquals(1, count.get());
	}
}
