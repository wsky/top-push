package com.tmall.top.push.mqtt;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.tmall.top.push.messages.Message;
import com.tmall.top.push.messages.MessageIO;
import com.tmall.top.push.mqtt.MqttVariableHeader.ReadWriteFlags;
import com.tmall.top.push.mqtt.publish.MqttPublishMessage;

public class MqttMessageIO {

	/*
	 * 
	 * 1byte Fixed Header:
	 * 
	 * MessageType/DUP/QOS/RETAIN RETAIN
	 * 
	 * 1-4byte Remaining length, max 256 MB
	 * 
	 * 
	 * PUBLISH
	 * 
	 * Variable Header: Topic Name/QoS level/Message ID
	 * 
	 * Payload:position=Fix-Header.length+Variable-Header.length
	 * 
	 * 1byte MessageType 8byte from(receiving)/to(sending) id, support
	 * front<-->forward<-->back.
	 * 
	 * 
	 * [X]PUBACK:
	 * 
	 * Variable Header: Message ID
	 */

	// payload act forward

	// server send: server -> client, write "from"
	// server receive: server <- client, read "to"

	// client send: client -> server, write "to"
	// client receive: client <- server, read "from"

	public static ByteBuffer parseServerSending(MqttMessage message,
			ByteBuffer buffer) {
		MqttPublishMessage pub = (MqttPublishMessage) message;
		buffer.position(0);

		// TODO:avoid rewrite mqtt header at server
		pub.Header.RemainingLength = MqttMessageIO
				.getVariableHeaderWriteLength(pub.VariableHeader)
				+ MessageIO.getFullMessageSize(pub.remainingLength);
		writeHeader(message.Header, buffer);
		writeVariableHeader(pub.VariableHeader, buffer);

		MessageIO.writeMessageType(buffer, message.messageType);
		MessageIO.writeClientId(buffer, message.from);
		MessageIO.writeRemainingLength(buffer, message.remainingLength);

		return buffer;
	}

	public static Message parseServerReceiving(MqttMessage message,
			ByteBuffer buffer) {
		MqttPublishMessage pub = (MqttPublishMessage) message;
		buffer.position(0);

		readHeader(message.Header, buffer);
		readVariableHeader(pub.VariableHeader, buffer);

		message.messageType = MessageIO.readMessageType(buffer);
		message.to = MessageIO.readClientId(buffer);
		message.remainingLength = MessageIO.readRemainingLength(buffer);
		message.fullMessageSize = getFullMessageSize(message);
		message.body = buffer;

		return message;
	}

	public static ByteBuffer parseClientSending(MqttMessage message,
			ByteBuffer buffer) {
		MqttPublishMessage pub = (MqttPublishMessage) message;
		buffer.position(0);

		pub.Header.RemainingLength = MqttMessageIO
				.getVariableHeaderWriteLength(pub.VariableHeader)
				+ MessageIO.getFullMessageSize(pub.remainingLength);
		
		writeHeader(message.Header, buffer);
		writeVariableHeader(pub.VariableHeader, buffer);
		
		MessageIO.writeMessageType(buffer, message.messageType);
		MessageIO.writeClientId(buffer, message.to);
		MessageIO.writeRemainingLength(buffer, message.remainingLength);

		return buffer;
	}

	public static Message parseClientReceiving(MqttMessage message,
			ByteBuffer buffer) {
		MqttPublishMessage pub = (MqttPublishMessage) message;
		buffer.position(0);

		readHeader(message.Header, buffer);
		readVariableHeader(pub.VariableHeader, buffer);

		message.messageType = MessageIO.readMessageType(buffer);
		message.from = MessageIO.readClientId(buffer);
		message.remainingLength = MessageIO.readRemainingLength(buffer);
		message.fullMessageSize = getFullMessageSize(message);
		message.body = buffer;

		return message;
	}

	public static int getFullMessageSize(MqttMessage message) {
		return message.Header.Length + message.Header.RemainingLength;
	}

	// MQTT parser simple implement

	public static int parseMessageType(byte b) {
		return (int) ((b & 240) >> 4);
	}

	public static MqttHeader readHeader(MqttHeader header, ByteBuffer buffer) {
		int firstHeaderByte = buffer.get();
		header.Retain = ((firstHeaderByte & 1) == 1 ? true : false);
		header.Qos = (int) ((firstHeaderByte & 6) >> 1);
		header.Duplicate = (((firstHeaderByte & 8) >> 3) == 1 ? true : false);
		header.MessageType = (int) ((firstHeaderByte & 240) >> 4);
		header.Length = 1;
		readRemainingLength(header, buffer);
		return header;
	}

	public static void writeHeader(MqttHeader header, ByteBuffer buffer) {
		int h = header.MessageType << 4;
		h += ((header.Duplicate ? 1 : 0) << 3);
		h += ((header.Qos << 1) + (header.Retain ? 1 : 0));
		buffer.put((byte) h);
		// headerStream
		// .put((byte) (
		// (header.MessageType << 4)
		// + ((header.Duplicate ? 1 : 0) << 3)
		// + ((header.Qos << 1) + (header.Retain ? 1 : 0))));
		header.Length = 1;
		writeRemainingLength(header, buffer);
	}

	public static void readRemainingLength(MqttHeader header, ByteBuffer buffer) {
		int remainingLength = 0;
		int multiplier = 1;
		byte sizeByte;
		int byteCount = 0;
		// read until got the entire size, or the 4 byte limit is reached
		do {
			sizeByte = buffer.get();
			remainingLength += (sizeByte & 0x7f) * multiplier;
			multiplier *= 0x80;
		} while (++byteCount <= 4 && (sizeByte & 0x80) == 0x80);
		header.RemainingLength = remainingLength;
		header.Length += byteCount;
	}

	public static void writeRemainingLength(MqttHeader header, ByteBuffer buffer) {
		int length = header.RemainingLength;
		do {
			int nextByteValue = length % 128;
			length = length / 128;
			if (length > 0) {
				nextByteValue = nextByteValue | 0x80;
			}
			buffer.put((byte) nextByteValue);
			header.Length += 1;
		} while (length > 0);
	}

	public static void writeVariableHeader(MqttVariableHeader header,
			ByteBuffer buffer) {
		int WriteFlags = header.getWriteFlags();
		if ((WriteFlags & ReadWriteFlags.ProtocolName) == ReadWriteFlags.ProtocolName)
			MqttMessageIO.writeMqttString(buffer, header.ProtocolName);
		if ((WriteFlags & ReadWriteFlags.ProtocolVersion) == ReadWriteFlags.ProtocolVersion)
			buffer.put(header.ProtocolVersion);
		if ((WriteFlags & ReadWriteFlags.ConnectFlags) == ReadWriteFlags.ConnectFlags)
			MqttMessageIO.writeConnectFlags(header.ConnectFlags, buffer);
		if ((WriteFlags & ReadWriteFlags.KeepAlive) == ReadWriteFlags.KeepAlive)
			buffer.putShort(header.KeepAlive);
		if ((WriteFlags & ReadWriteFlags.ReturnCode) == ReadWriteFlags.ReturnCode)
			buffer.put((byte) header.ReturnCode);
		if ((WriteFlags & ReadWriteFlags.TopicName) == ReadWriteFlags.TopicName)

			MqttMessageIO.writeMqttString(buffer, header.TopicName);
		if ((WriteFlags & ReadWriteFlags.MessageIdentifier) == ReadWriteFlags.MessageIdentifier)
			buffer.putShort(header.MessageIdentifier);
	}

	public static void readVariableHeader(MqttVariableHeader header,
			ByteBuffer buffer) {
		int ReadFlags = header.getReadFlags();
		if ((ReadFlags & ReadWriteFlags.ProtocolName) == ReadWriteFlags.ProtocolName) {
			header.ProtocolName = MqttMessageIO.readMqttString(buffer);
			header.Length += header.ProtocolName.length() + 2;
		}
		if ((ReadFlags & ReadWriteFlags.ProtocolVersion) == ReadWriteFlags.ProtocolVersion) {
			header.ProtocolVersion = buffer.get();
			header.Length++;
		}
		if ((ReadFlags & ReadWriteFlags.ConnectFlags) == ReadWriteFlags.ConnectFlags) {
			MqttMessageIO.readConnectFlags(header.ConnectFlags, buffer);
			header.Length += 1;
		}
		if ((ReadFlags & ReadWriteFlags.KeepAlive) == ReadWriteFlags.KeepAlive) {
			header.KeepAlive = buffer.getShort();
			header.Length += 2;
		}
		if ((ReadFlags & ReadWriteFlags.ReturnCode) == ReadWriteFlags.ReturnCode) {
			header.ReturnCode = (int) buffer.get();
			header.Length++;
		}
		if ((ReadFlags & ReadWriteFlags.TopicName) == ReadWriteFlags.TopicName) {
			header.TopicName = MqttMessageIO.readMqttString(buffer);
			header.Length += header.TopicName.length() + 2;
		}
		if ((ReadFlags & ReadWriteFlags.MessageIdentifier) == ReadWriteFlags.MessageIdentifier) {
			header.MessageIdentifier = buffer.getShort();
			header.Length += 2;
		}
	}

	public static int getVariableHeaderWriteLength(MqttVariableHeader header) {
		int headerLength = 0;
		int WriteFlags = header.getWriteFlags();
		if ((WriteFlags & ReadWriteFlags.ProtocolName) == ReadWriteFlags.ProtocolName)
			headerLength += MqttMessageIO.getByteCount(header.ProtocolName);
		if ((WriteFlags & ReadWriteFlags.ProtocolVersion) == ReadWriteFlags.ProtocolVersion)
			headerLength += 1;
		if ((WriteFlags & ReadWriteFlags.ConnectFlags) == ReadWriteFlags.ConnectFlags)
			headerLength += MqttMessageIO.getConnectFlagsLength();
		if ((WriteFlags & ReadWriteFlags.KeepAlive) == ReadWriteFlags.KeepAlive)
			headerLength += 2;
		if ((WriteFlags & ReadWriteFlags.ReturnCode) == ReadWriteFlags.ReturnCode)
			headerLength += 1;
		if ((WriteFlags & ReadWriteFlags.TopicName) == ReadWriteFlags.TopicName)
			headerLength += MqttMessageIO.getByteCount(header.TopicName);
		if ((WriteFlags & ReadWriteFlags.MessageIdentifier) == ReadWriteFlags.MessageIdentifier)
			headerLength += 2;
		return headerLength;
	}

	private static int getConnectFlagsLength() {
		return 1;
	}

	public static void writeConnectFlags(MqttConnectFlags flags,
			ByteBuffer buffer) {
		byte b = (byte) ((flags.Reserved1 ? 1 : 0)
				| (flags.CleanStart ? 1 : 0) << 1
				| (flags.WillFlag ? 1 : 0) << 2 | ((byte) flags.WillQos) << 3
				| (flags.WillRetain ? 1 : 0) << 5
				| (flags.Reserved2 ? 1 : 0) << 6 | (flags.Reserved3 ? 1 : 0) << 7);
		buffer.put(b);
	}

	public static MqttConnectFlags readConnectFlags(MqttConnectFlags flags,
			ByteBuffer buffer) {
		byte connectFlagsByte = buffer.get();
		flags.Reserved1 = (connectFlagsByte & 1) == 1;
		flags.CleanStart = (connectFlagsByte & 2) == 2;
		flags.WillFlag = (connectFlagsByte & 4) == 4;
		flags.WillQos = (connectFlagsByte >> 3) & 3;
		flags.WillRetain = (connectFlagsByte & 32) == 32;
		flags.Reserved2 = (connectFlagsByte & 64) == 64;
		flags.Reserved3 = (connectFlagsByte & 128) == 128;
		return flags;
	}

	public static String readMqttString(ByteBuffer buffer) {
		int l = (buffer.get() << 8) + buffer.get();
		// int msb = buffer.get() & 0x00FF;
		// int lsb = buffer.get() & 0x00FF;
		// msb = (msb << 8) | lsb;
		String str = new String(buffer.array(), buffer.position(), l,
				Charset.forName("UTF-8"));
		buffer.position(buffer.position() + l);
		return str;
	}

	public static void writeMqttString(ByteBuffer buffer, String value) {
		if (value == null || value == "") {
			buffer.put((byte) (0 >> 8));
			buffer.put((byte) (0 & 0xFF));
			return;
		}
		try {
			byte[] bytes = value.getBytes("UTF-8");
			buffer.put((byte) (bytes.length >> 8));
			buffer.put((byte) (bytes.length & 0xFF));
			buffer.put(bytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static int getByteCount(String value) {
		if (value == null || value == "")
			return 2;
		// TODO: avoid getBytes create temp array
		try {
			return value.getBytes("UTF-8").length + 2;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			// ASCII
			return value.length() + 2;
		}
	}
}