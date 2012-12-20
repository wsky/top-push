package com.tmall.top.push;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import org.junit.Test;

import com.tmall.top.push.MessageTooLongException;
import com.tmall.top.push.MessageTypeNotSupportException;
import com.tmall.top.push.NoMessageBufferException;
import com.tmall.top.push.Receiver;
import com.tmall.top.push.messages.Message;
import com.tmall.top.push.messages.MessageIO;
import com.tmall.top.push.messages.PublishConfirmMessage;
import com.tmall.top.push.messages.PublishMessage;

public class ReceiverTest {
	@Test
	public void parse_acquire_release_publish_message_test()
			throws MessageTooLongException, MessageTypeNotSupportException,
			NoMessageBufferException {
		// prepare
		byte[] back = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(back);
		PublishMessage msgPublish = new PublishMessage();
		msgPublish.to = "abc";
		msgPublish.remainingLength = 100;
		MessageIO.parseClientSending(msgPublish, buffer);
		// acquire parse to message
		Receiver receiver = new Receiver(1024, 1024, 10, 10);
		Message msg = receiver.parseMessage("", buffer.array(), 0, 1024);
		assertEquals(PublishMessage.class, msg.getClass());
		// assert more about buffer
		assertEquals(1024, ((ByteBuffer) msg.body).limit());
		// parse to buffer
		ByteBuffer buffer2 = receiver.parseMessage("", msg);
		assertEquals(buffer.hashCode(), buffer2.hashCode());
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

	@Test
	public void parse_acquire_release_confirm_message_test()
			throws MessageTooLongException, MessageTypeNotSupportException,
			NoMessageBufferException {
		// prepare
		byte[] back = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(back);
		PublishConfirmMessage msgConfirm = new PublishConfirmMessage();
		msgConfirm.to = "abc";
		msgConfirm.remainingLength = 100;
		MessageIO.parseClientSending(msgConfirm, buffer);
		// acquire parse to message
		Receiver receiver = new Receiver(1024, 1024, 10, 10);
		Message msg = receiver.parseMessage("", buffer.array(), 0, 1024);
		assertNotSame(msgConfirm, msg);
		assertEquals(PublishConfirmMessage.class, msg.getClass());
		// parse to buffer
		ByteBuffer buffer2 = receiver.parseMessage("", msg);
		assertEquals(buffer, buffer2);
		// release
		receiver.release(msg);
		assertNull(msg.to);
		assertNull(msg.from);
		assertNull(msg.body);
		assertEquals(0, msg.remainingLength);
		assertEquals(0, msg.fullMessageSize);
		// reuse from object pool
		Message msg2 = receiver.parseMessage("", buffer.array(), 0, 1024);
		assertEquals(msg, msg2);
	}

	@Test(expected = NoMessageBufferException.class)
	public void parse_and_no_buffer_test() throws MessageTooLongException,
			MessageTypeNotSupportException, NoMessageBufferException {
		Receiver receiver = new Receiver(1024, 1024, 0, 0);
		receiver.parseMessage("", new byte[] { 1 }, 0, 1);
	}

	@Test(expected = MessageTooLongException.class)
	public void parse_and_message_too_long_test()
			throws MessageTooLongException, MessageTypeNotSupportException,
			NoMessageBufferException {
		Receiver receiver = new Receiver(10, 10, 1, 1);
		receiver.parseMessage("", new byte[] { 1, 2, 2, 2 }, 0, 20);
	}

	@Test(expected = MessageTypeNotSupportException.class)
	public void parse_and_invalid_message_type_test()
			throws MessageTooLongException, MessageTypeNotSupportException,
			NoMessageBufferException {
		Receiver receiver = new Receiver(1024, 1024, 1, 1);
		receiver.parseMessage("", new byte[] { 0 }, 0, 1);
	}

	@Test
	public void parse_message_test() {

	}
}
