package com.tmall.top.push;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.tmall.top.push.messages.Message;
import com.tmall.top.push.messages.MessageIO;
import com.tmall.top.push.mqtt.MqttMessage;
import com.tmall.top.push.mqtt.MqttMessageIO;
import com.tmall.top.push.mqtt.MqttMessageType;
import com.tmall.top.push.mqtt.connect.MqttConnectMessage;
import com.tmall.top.push.mqtt.disconnect.MqttDisconnectMessage;
import com.tmall.top.push.mqtt.publish.MqttPublishMessage;

// provide message parser, receiving-buffer and improvement
public class Receiver {
	private static final String MQTT = "mqtt";
	private int maxMessageSize;

	private DefaultMessagePool defaultMessagePool;
	private MqttPublishMessagePool mqttPublishMessagePool;
	private byte[] buffer;
	private ConcurrentLinkedQueue<ByteBuffer> bufferQueue;

	public Receiver(int maxMessageSize, int maxMessageBufferCount) {
		this.maxMessageSize = maxMessageSize;
		// object pool
		this.defaultMessagePool = new DefaultMessagePool(maxMessageBufferCount);
		this.mqttPublishMessagePool = new MqttPublishMessagePool(
				maxMessageBufferCount);
		// buffer
		this.bufferQueue = new ConcurrentLinkedQueue<ByteBuffer>();
		this.buffer = new byte[this.maxMessageSize * maxMessageBufferCount];
		// fill message-buffer queue
		this.fillBufferQueue(this.bufferQueue, this.buffer,
				this.maxMessageSize, maxMessageBufferCount);
	}

	// must be called after send
	public synchronized void release(Message message) {
		// return buffer for reusing
		if (message.body != null && message.body instanceof ByteBuffer) {
			this.bufferQueue.add((ByteBuffer) message.body);
		}

		message.clear();

		if (message instanceof MqttPublishMessage) {
			this.mqttPublishMessagePool.release((MqttPublishMessage) message);
		} else {
			this.defaultMessagePool.release(message);
		}
	}

	// for receiving message from lower buffer
	public Message parseMessage(String protocol, byte[] data, int offset,
			int length) throws MessageTooLongException,
			NoMessageBufferException {
		ByteBuffer buffer = this.getBuffer(length);
		Message msg = this.acquireMessage(protocol, data[offset]);

		if (buffer != null) {
			buffer.put(data, offset, length);
			this.parseMessage(protocol, msg, buffer);
		} else {
			throw new NoMessageBufferException();
		}

		return msg;
	}

	// for send message to lower buffer
	public ByteBuffer parseMessage(String protocol, Message message) {
		if (MQTT.equalsIgnoreCase(protocol)) {
			return MqttMessageIO.parseServerSending((MqttMessage) message,
					(ByteBuffer) message.body);
		} else {
			return MessageIO.parseServerSending(message,
					(ByteBuffer) message.body);
		}
	}

	private void parseMessage(String protocol, Message message,
			ByteBuffer buffer) {
		if (MQTT.equalsIgnoreCase(protocol)) {
			MqttMessageIO.parseServerReceiving((MqttMessage) message, buffer);
		} else {
			MessageIO.parseServerReceiving(message, buffer);
		}
	}

	private Message acquireMessage(String protocol, byte firstHeaderByte) {
		if (MQTT.equalsIgnoreCase(protocol)) {
			int messageType = MqttMessageIO.parseMessageType(firstHeaderByte);
			if (messageType == MqttMessageType.Connect)
				return new MqttConnectMessage();
			else if (messageType == MqttMessageType.Disconnect)
				return new MqttDisconnectMessage();
			else
				return this.mqttPublishMessagePool.acquire();
		} else {
			return this.defaultMessagePool.acquire();
		}
	}

	private ByteBuffer getBuffer(int length) throws MessageTooLongException {
		if (length > this.maxMessageSize)
			throw new MessageTooLongException();
		// TODO: if no buffer, retry twice with lock-free?
		ByteBuffer buffer = this.bufferQueue.poll();
		if (buffer != null)
			buffer.position(0);
		return buffer;
	}

	private void fillBufferQueue(ConcurrentLinkedQueue<ByteBuffer> bufferQueue,
			byte[] buffer, int size, int count) {
		for (int i = 0; i < count; i++) {
			bufferQueue.add(ByteBuffer.wrap(buffer, i * size, size).slice());
		}
	}

	class DefaultMessagePool extends Pool<Message> {

		public DefaultMessagePool(int poolSize) {
			super(poolSize);
		}

		@Override
		public Message createNew() {
			return new Message();
		}

	}

	class MqttPublishMessagePool extends Pool<MqttPublishMessage> {

		public MqttPublishMessagePool(int poolSize) {
			super(poolSize);
		}

		@Override
		public MqttPublishMessage createNew() {
			// HACK:use Publish as forward message
			return new MqttPublishMessage();
		}

	}
}
