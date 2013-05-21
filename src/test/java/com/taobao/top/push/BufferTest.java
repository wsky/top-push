package com.taobao.top.push;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.junit.Test;

public class BufferTest {
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
}
