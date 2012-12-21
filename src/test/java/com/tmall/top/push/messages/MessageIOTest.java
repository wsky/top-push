package com.tmall.top.push.messages;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.tmall.top.push.messages.MessageIO;
import com.tmall.top.push.messages.MessageType;
import com.tmall.top.push.messages.PublishMessage;

public class MessageIOTest {

	@Test
	public void read_write_message_type_test() {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		MessageIO.writeMessageType(buffer, MessageType.PUBLISH);
		buffer.position(0);
		assertEquals(MessageType.PUBLISH, MessageIO.readMessageType(buffer));
	}

	@Test
	public void read_write_client_id_test() {
		// always 8
		assertEquals(" abcdefg", MessageIO.padClientId("abcdefg"));

		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		MessageIO.writeClientId(buffer, "abc");
		buffer.position(0);
		assertEquals("abc", MessageIO.readClientId(buffer));
	}

	@Test
	public void read_write_string_test() {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		MessageIO.writeString(buffer, "abc");
		assertEquals(3, buffer.position());
		buffer.position(0);
		assertEquals("abc", MessageIO.readString(buffer, 3));
	}

	@Test
	public void client_to_server_parse_test() {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		PublishMessage msg = new PublishMessage();
		msg.messageType = MessageType.PUBLISH;
		msg.to = "abc";
		msg.remainingLength = 100;

		MessageIO.parseClientSending(msg, buffer);
		msg.clear();
		
		MessageIO.parseServerReceiving(msg, buffer);
		assertEquals(MessageType.PUBLISH, msg.messageType);
		assertEquals("abc", msg.to);
		assertEquals(100, msg.remainingLength);
	}

	@Test
	public void server_to_client_parse_test() {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		PublishMessage msg = new PublishMessage();
		msg.messageType = MessageType.PUBLISH;
		msg.from = "abc";
		msg.remainingLength = 100;

		MessageIO.parseServerSending(msg, buffer);
		msg.clear();
		
		MessageIO.parseClientReceiving(msg, buffer);
		System.out.println(msg.to);
		assertEquals(MessageType.PUBLISH, msg.messageType);
		assertEquals("abc", msg.from);
		assertEquals(100, msg.remainingLength);
	}
}
