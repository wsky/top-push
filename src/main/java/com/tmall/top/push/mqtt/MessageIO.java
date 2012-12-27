package com.tmall.top.push.mqtt;

import java.nio.ByteBuffer;

import com.tmall.top.push.messages.Message;
import com.tmall.top.push.mqtt.MqttVariableHeader.ReadWriteFlags;

public class MessageIO {

	public static ByteBuffer parseServerSending(Message message,
			ByteBuffer buffer) {
		buffer.position(0);
		// writeMessageType(buffer, message.messageType);
		// writeClientId(buffer, message.from);
		buffer.putInt(message.remainingLength);
		return buffer;
	}

	public static Message parseServerReceiving(Message message,
			ByteBuffer buffer) {
		buffer.position(0);
		// message.messageType = readMessageType(buffer);
		// message.to = readClientId(buffer);
		message.remainingLength = buffer.getInt();
		// message.fullMessageSize =
		// getFullMessageSize(message.remainingLength);
		message.body = buffer;
		return message;
	}

	public static MqttHeader readHeader(MqttHeader header,
			ByteBuffer headerStream) {
		int firstHeaderByte = headerStream.get();
		header.Retain = ((firstHeaderByte & 1) == 1 ? true : false);
		header.Qos = (int) ((firstHeaderByte & 6) >> 1);
		header.Duplicate = (((firstHeaderByte & 8) >> 3) == 1 ? true : false);
		header.MessageType = (int) ((firstHeaderByte & 240) >> 4);
		header.RemainingLength = readRemainingLength(headerStream);
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
		writeRemainingLength(header.RemainingLength, buffer);
	}

	public static int readRemainingLength(ByteBuffer buffer) {
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
		return remainingLength;
	}

	public static void writeRemainingLength(int length, ByteBuffer buffer) {
		do {
			int nextByteValue = length % 128;
			length = length / 128;
			if (length > 0) {
				nextByteValue = nextByteValue | 0x80;
			}
			buffer.put((byte) nextByteValue);
		} while (length > 0);
	}

	public static void writeVariableHeader(MqttVariableHeader header,
			ByteBuffer buffer) {
		int WriteFlags = header.getWriteFlags();
		if ((WriteFlags & ReadWriteFlags.ProtocolName) == ReadWriteFlags.ProtocolName)
			MessageIO.writeMqttString(buffer, header.ProtocolName);
		if ((WriteFlags & ReadWriteFlags.ProtocolVersion) == ReadWriteFlags.ProtocolVersion)
			buffer.put(header.ProtocolVersion);
		if ((WriteFlags & ReadWriteFlags.ConnectFlags) == ReadWriteFlags.ConnectFlags)
			MessageIO.writeConnectFlags(header.ConnectFlags, buffer);
		if ((WriteFlags & ReadWriteFlags.KeepAlive) == ReadWriteFlags.KeepAlive)
			buffer.putShort(header.KeepAlive);
		if ((WriteFlags & ReadWriteFlags.ReturnCode) == ReadWriteFlags.ReturnCode)
			buffer.put((byte) header.ReturnCode);
		if ((WriteFlags & ReadWriteFlags.TopicName) == ReadWriteFlags.TopicName)

			MessageIO.writeMqttString(buffer, header.TopicName);
		if ((WriteFlags & ReadWriteFlags.MessageIdentifier) == ReadWriteFlags.MessageIdentifier)
			buffer.putShort(header.MessageIdentifier);
	}

	public static void readVariableHeader(MqttVariableHeader header,
			ByteBuffer buffer) {
		int ReadFlags = header.getReadFlags();
		if ((ReadFlags & ReadWriteFlags.ProtocolName) == ReadWriteFlags.ProtocolName) {
			header.ProtocolName = MessageIO.readMqttString(buffer);
			header.Length += header.ProtocolName.length() + 2;
		}
		if ((ReadFlags & ReadWriteFlags.ProtocolVersion) == ReadWriteFlags.ProtocolVersion) {
			header.ProtocolVersion = buffer.get();
			header.Length++;
		}
		if ((ReadFlags & ReadWriteFlags.ConnectFlags) == ReadWriteFlags.ConnectFlags) {
			MessageIO.readConnectFlags(header.ConnectFlags, buffer);
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
			header.TopicName = MessageIO.readMqttString(buffer);
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
			headerLength += MessageIO.getByteCount(header.ProtocolName);
		if ((WriteFlags & ReadWriteFlags.ProtocolVersion) == ReadWriteFlags.ProtocolVersion)
			headerLength += 1;
		if ((WriteFlags & ReadWriteFlags.ConnectFlags) == ReadWriteFlags.ConnectFlags)
			headerLength += MessageIO.getConnectFlagsLength();
		if ((WriteFlags & ReadWriteFlags.KeepAlive) == ReadWriteFlags.KeepAlive)
			headerLength += 2;
		if ((WriteFlags & ReadWriteFlags.ReturnCode) == ReadWriteFlags.ReturnCode)
			headerLength += 1;
		if ((WriteFlags & ReadWriteFlags.TopicName) == ReadWriteFlags.TopicName)
			headerLength += MessageIO.getByteCount(header.TopicName);
		if ((WriteFlags & ReadWriteFlags.MessageIdentifier) == ReadWriteFlags.MessageIdentifier)
			headerLength += 2;
		return headerLength;
	}

	private static int getConnectFlagsLength() {
		return 1;
	}

	private static void writeConnectFlags(MqttConnectFlags flags,
			ByteBuffer buffer) {
		byte b = (byte) ((flags.Reserved1 ? 1 : 0)
				| (flags.CleanStart ? 1 : 0) << 1
				| (flags.WillFlag ? 1 : 0) << 2 | ((byte) flags.WillQos) << 3
				| (flags.WillRetain ? 1 : 0) << 5
				| (flags.Reserved2 ? 1 : 0) << 6 | (flags.Reserved3 ? 1 : 0) << 7);
		buffer.put(b);
	}

	private static MqttConnectFlags readConnectFlags(MqttConnectFlags flags,
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

	private static String readMqttString(ByteBuffer buffer) {
		byte[] lengthBytes = new byte[2];
		buffer.get(lengthBytes, 0, 2);
		short stringLength = (short) ((lengthBytes[0] << 8) + lengthBytes[1]);
		byte[] stringBytes = new byte[stringLength];
		buffer.get(stringBytes, 0, stringLength);
		return new String(stringBytes); // "ASCII");
	}

	private static void writeMqttString(ByteBuffer buffer, String value) {
		int l = value.length();
		buffer.put((byte) (l >> 8));
		buffer.put((byte) (l & 0xFF));
		for (int i = 0; i < l; i++)
			buffer.put((byte) value.charAt(i));
	}

	private static int getByteCount(String value) {
		return value.length() + 2;
		// FIXME: mqtt string encoding
		// value.getBytes(Charset.forName(""))
	}
}
