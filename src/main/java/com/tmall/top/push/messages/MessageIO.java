package com.tmall.top.push.messages;

import java.nio.ByteBuffer;

// parse message using default protocol
public final class MessageIO {

	/*
	 * 1byte MessageType, or extend to 8bit usage, DUP Flag, Body Formatter,
	 * RETAIN
	 * 
	 * 8byte from(receiving)/to(sending) id, support front<-->forward<-->back.
	 * 
	 * 4byte remainingLength int32, do not need support longer message.
	 * 
	 * ... body/content, maybe serialized by json/protobuf/msgpack/...
	 * 
	 * body example, just resolve/parse by client: - publish message: {
	 * MessageId:"20121221000000001", Content:"hello world!" } - confirm
	 * message: ["20121221000000001", "20121221000000002"]
	 */

	// server send: server -> client, write "from"
	// server receive: server <- client, read "to"

	// client send: client -> server, write "to"
	// client receive: client <- server, read "from"

	public static ByteBuffer parseServerSending(Message message,
			ByteBuffer buffer) {
		buffer.position(0);
		writeMessageType(buffer, message.messageType);
		writeClientId(buffer, message.from);
		buffer.putInt(message.remainingLength);
		return buffer;
	}

	public static Message parseServerReceiving(Message message,
			ByteBuffer buffer) {
		buffer.position(0);
		message.messageType = readMessageType(buffer);
		message.to = readClientId(buffer);
		message.remainingLength = buffer.getInt();
		message.fullMessageSize = getFullMessageSize(message.remainingLength);
		message.body = buffer;
		return message;
	}

	public static ByteBuffer parseClientSending(Message message,
			ByteBuffer buffer) {
		buffer.position(0);
		writeMessageType(buffer, message.messageType);
		writeClientId(buffer, message.to);
		buffer.putInt(message.remainingLength);
		// HACK: body serialize to buffer, if client
		// do not care on server, just process at client
		return buffer;
	}

	public static Message parseClientReceiving(Message message,
			ByteBuffer buffer) {
		buffer.position(0);
		message.messageType = readMessageType(buffer);
		message.from = readClientId(buffer);
		message.remainingLength = buffer.getInt();
		message.fullMessageSize = getFullMessageSize(message.remainingLength);
		message.body = buffer;
		return message;
	}

	public static int parseMessageType(byte headerByte) {
		return headerByte;
	}

	public static int readMessageType(ByteBuffer buffer) {
		return buffer.get();
	}

	public static void writeMessageType(ByteBuffer buffer, int messageType) {
		buffer.put((byte) messageType);
	}

	public static String readClientId(ByteBuffer buffer) {
		return readString(buffer, 8).trim();
	}

	public static void writeClientId(ByteBuffer buffer, String id) {
		writeString(buffer, padClientId(id));
	}

	// TODO:string encoding? a-zA-Z0-9 not necessary
	public static String readString(ByteBuffer buffer, int length) {
		String value = "";
		for (int i = 0; i < length; i++)
			value += (char) buffer.get();
		return value;
	}

	public static void writeString(ByteBuffer buffer, String value) {
		for (int i = 0; i < value.length(); i++)
			buffer.put((byte) value.charAt(i));
	}

	public static String padClientId(String id) {
		return String.format("%8s", id);
	}

	private static int getFullMessageSize(int remainingLength) {
		return remainingLength + 1 + 8 + 4;
	}

}