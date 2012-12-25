package com.tmall.top.push;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
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

		byteBuffer.position(0);
		assertEquals(0, byteBuffer.position());
		byteBuffer.putInt(100);
		assertEquals(4, byteBuffer.position());
	}

	@Test
	public void nio_buffer_position_slice_test() {
		byte[] buffer = new byte[1024];
		// new wrapper
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

	@Test
	public void nio_buffer_put_string_test()
			throws UnsupportedEncodingException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

		byte[] bytes = "abc".getBytes();
		assertEquals(3, bytes.length);
		assertEquals(bytes[0], "abc".charAt(0));
		assertEquals(bytes[1], "abc".charAt(1));
		assertEquals(bytes[2], "abc".charAt(2));

		buffer.put(bytes);
		buffer.position(0);
		buffer.get(bytes, 0, bytes.length);
		assertEquals("abc", new String(bytes, "UTF-8"));
	}

	@Test
	public void nio_buffer_put_int_test() {
		byte[] bytes = new byte[4]; //{ 28, 00, 00, 00 };
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.putInt(40);
		buffer.position(0);
		assertEquals(40, buffer.getInt());
		for(int i =0;i<bytes.length;i++)
			System.out.println((int)bytes[i]);
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
