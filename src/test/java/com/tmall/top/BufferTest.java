package com.tmall.top;

import static org.junit.Assert.*;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;

public class BufferTest {

	private static int SIZE = 1024;

	@Test
	public void message_buffer_add_remove_perf_test() {
		int total = 600000;
		// 1K Message
		String msg = this.GetItem(SIZE);
		// CircularFifoBuffer is a first in first out buffer with a fixed size
		// that replaces its oldest element if full.
		CircularFifoBuffer buffer = new CircularFifoBuffer(total);

		StopWatch watch = new StopWatch();
		watch.start();

		for (int i = 0; i < total; i++) {
			buffer.add(msg);
		}

		watch.stop();
		System.out.println(String.format("add %s messages cost %sms", total,
				watch.getTime()));

		watch = new StopWatch();
		watch.start();
		for (int i = 0; i < total; i++) {
			buffer.remove();
		}
		watch.stop();
		System.out.println(String.format("remove %s messages cost %sms", total,
				watch.getTime()));
	}

	@Test
	public void message_buffer_add_more_test() {
		int total = 600000;
		// 1K Message
		String msg = this.GetItem(SIZE);
		CircularFifoBuffer buffer = new CircularFifoBuffer(total);

		StopWatch watch = new StopWatch();
		watch.start();

		for (int i = 0; i < total * 2; i++) {
			buffer.add(msg);
		}

		watch.stop();
		System.out.println(String.format("add %s messages cost %sms",
				total * 2, watch.getTime()));
	}

	@Test
	public void message_buffer_add_remove_concurrent_simply_test()
			throws InterruptedException {
		final int total = 600000;
		// 1K Message
		final String msg = this.GetItem(SIZE);
		final CircularFifoBuffer buffer = new CircularFifoBuffer(total);

		for (int i = 0; i < total; i++) {
			// not thread safe
			buffer.add(msg);
		}

		Thread writer = new Thread() {
			public void run() {
				StopWatch watch = new StopWatch();
				watch.start();
				for (int i = 0; i < total; i++) {
					buffer.add(msg);
				}
				watch.stop();
				System.out.println(String.format("add %s messages cost %sms",
						total, watch.getTime()));
			}
		};
		Thread reader = new Thread() {
			public void run() {
				StopWatch watch = new StopWatch();
				watch.start();
				for (int i = 0; i < total; i++) {
					if (!buffer.isEmpty())
						buffer.remove();

				}
				watch.stop();
				System.out
						.println(String.format("remove %s messages cost %sms",
								total, watch.getTime()));
			}
		};

		writer.start();
		reader.start();
		Thread.sleep(2000);
	}

	@Test
	public void nio_buffer_position_test() {
		byte[] buffer = new byte[1024];
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		assertEquals(buffer, byteBuffer.array());
		
		assertEquals(0, byteBuffer.arrayOffset());
		assertEquals(0, byteBuffer.position());
		assertEquals(1024, byteBuffer.capacity());
		assertEquals(1024, byteBuffer.limit());
		
		byteBuffer.putInt(100);
		assertEquals(4, byteBuffer.position());
		
	}

	@Test
	public void nio_buffer_position_slice_test() {
		byte[] buffer = new byte[1024];
		//new wrapper
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 10, 30).slice();

		assertEquals(10, byteBuffer.arrayOffset());
		assertEquals(0, byteBuffer.position());
		assertEquals(30, byteBuffer.capacity());
		assertEquals(30, byteBuffer.limit());
		
		byteBuffer.putInt(100);
		assertEquals(10, byteBuffer.arrayOffset());
		assertEquals(4, byteBuffer.position());
		assertEquals(30, byteBuffer.capacity());
		assertEquals(30, byteBuffer.limit());
		
		byteBuffer.position(0);
		assertEquals(10, byteBuffer.arrayOffset());
		assertEquals(0, byteBuffer.position());
		assertEquals(30, byteBuffer.capacity());
		assertEquals(30, byteBuffer.limit());
	}

	// private String[] GetItems(int count, int size) {
	// String[] items = new String[count];
	// for (int i = 0; i < count; i++)
	// items[i] = this.GetItem(size);
	// return items;
	// }

	private String GetItem(int size) {
		String string = "";
		for (int i = 0; i < size; i++)
			string += "i";
		return string;
	}

}
