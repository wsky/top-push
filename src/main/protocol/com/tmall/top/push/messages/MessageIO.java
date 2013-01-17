package com.tmall.top.push.messages;

import java.nio.ByteBuffer;

// parse message using default protocol
public final class MessageIO {

	/*
	 * 1byte MessageType, or extend to 8bit usage, DUP Flag, Body Formatter,
	 * RETAIN
	 * 
	 * 8 bytes from(receiving)/to(sending) id, support front<-->forward<-->back.
	 * 
	 * 1byte Message body format flag
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
		writeBodyFormat(buffer, message.bodyFormat);
		writeRemainingLength(buffer, message.remainingLength);
		return buffer;
	}

	public static Message parseServerReceiving(Message message,
			ByteBuffer buffer) {
		buffer.position(0);
		message.messageType = readMessageType(buffer);
		message.to = readClientId(buffer);
		message.bodyFormat = readBodyFormat(buffer);
		message.remainingLength = readRemainingLength(buffer);
		message.fullMessageSize = getFullMessageSize(message.remainingLength);
		message.body = buffer;
		return message;
	}

	public static ByteBuffer parseClientSending(Message message,
			ByteBuffer buffer) {
		buffer.position(0);
		writeMessageType(buffer, message.messageType);
		writeClientId(buffer, message.to);
		writeBodyFormat(buffer, message.bodyFormat);
		writeRemainingLength(buffer, message.remainingLength);
		// HACK: body serialize to buffer, if client
		// do not care on server, just process at client
		return buffer;
	}

	public static Message parseClientReceiving(Message message,
			ByteBuffer buffer) {
		buffer.position(0);
		message.messageType = readMessageType(buffer);
		message.from = readClientId(buffer);
		message.bodyFormat = readBodyFormat(buffer);
		message.remainingLength = readRemainingLength(buffer);
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

	public static int readBodyFormat(ByteBuffer buffer) {
		return buffer.get();
	}

	public static void writeBodyFormat(ByteBuffer buffer, int bodyFormat) {
		buffer.put((byte) bodyFormat);
	}

	public static int readRemainingLength(ByteBuffer buffer) {
		return buffer.getInt();
	}

	public static void writeRemainingLength(ByteBuffer buffer,
			int remainingLength) {
		buffer.putInt(remainingLength);
	}

	// HACK:string encoding? a-zA-Z0-9 not necessary
	public static String readString(ByteBuffer buffer, int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
			sb.append((char) buffer.get());
		return sb.toString();
	}

	public static void writeString(ByteBuffer buffer, String value) {
		int l = value.length();
		for (int i = 0; i < l; i++)
			buffer.put((byte) value.charAt(i));
	}

	public static String padClientId(String id) {
		// HACK:8 is faster!
		if (id != null && id.length() == 8)
			return id;
		return String.format("%8s", id);// bad perf
	}

	public static int getFullMessageSize(int remainingLength) {
		return remainingLength + 1 + 8 + 1 + 4;
	}
}