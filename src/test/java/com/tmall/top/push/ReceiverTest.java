package com.tmall.top.push;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import org.junit.Test;

import com.tmall.top.push.MessageTooLongException;
import com.tmall.top.push.NoMessageBufferException;
import com.tmall.top.push.Receiver;
import com.tmall.top.push.messages.Message;
import com.tmall.top.push.messages.MessageIO;

public class ReceiverTest {
	@Test
	public void parse_acquire_release_message_test()
			throws MessageTooLongException, NoMessageBufferException {
		// prepare
		byte[] back = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(back);
		Message msg = new Message();
		msg.to = "abc";
		msg.remainingLength = 100;
		MessageIO.parseClientSending(msg, buffer);
		// acquire parse to message
		Receiver receiver = new Receiver(1024, 10);
		msg = receiver.parseMessage("", buffer.array(), 0, 1024);
		assertEquals(Message.class, msg.getClass());
		// assert more about buffer
		assertEquals(1024, ((ByteBuffer) msg.body).limit());
		// parse to buffer
		// ByteBuffer buffer2 = receiver.parseMessage("", msg);
		// release
		receiver.release(msg);
		assertNull(msg.to);
		assertNull(msg.from);
		assertNull(msg.body);
		assertEquals(0, msg.remainingLength);
		assertEquals(0, msg.fullMessageSize);
		// reuse from object pool
		Message msg2 = receiver.parseMessage("", buffer.array(), 0, 1024);
		assertEquals(msg.hashCode(), msg2.hashCode());
		assertEquals(msg.body, msg2.body);
	}

	@Test(expected = NoMessageBufferException.class)
	public void parse_and_no_buffer_test() throws MessageTooLongException,
			NoMessageBufferException {
		Receiver receiver = new Receiver(1024, 0);
		receiver.parseMessage("", new byte[] { 1 }, 0, 1);
	}

	@Test(expected = MessageTooLongException.class)
	public void parse_and_message_too_long_test()
			throws MessageTooLongException, NoMessageBufferException {
		Receiver receiver = new Receiver(10, 1);
		receiver.parseMessage("", new byte[] { 1, 2, 2, 2 }, 0, 20);
	}
}
