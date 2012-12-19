package com.tmall.top.push;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.tmall.top.push.messages.Message;
import com.tmall.top.push.messages.MessageType;
import com.tmall.top.push.messages.PublishConfirmMessage;
import com.tmall.top.push.messages.PublishConfirmMessagePool;
import com.tmall.top.push.messages.PublishMessage;
import com.tmall.top.push.messages.PublishMessagePool;

public class Receiver {
	private int publishMessageSize;
	private int confirmMessageSize;

	private PublishMessagePool publishMessagePool;
	private PublishConfirmMessagePool confirmMessagePool;
	private byte[] publishBuffer;
	private byte[] confirmBuffer;
	private ConcurrentLinkedQueue<ByteBuffer> publishBufferQueue;
	private ConcurrentLinkedQueue<ByteBuffer> confirmBufferQueue;

	// provide message receiving buffer and improvement
	public Receiver(int publishMessageSize, int confirmMessageSize,
			int publishMessageBufferCount, int confirmMessageBufferCount) {
		// message size
		this.publishMessageSize = publishMessageSize;
		this.confirmMessageSize = confirmMessageSize;
		// object pool
		// is it necessary?
		this.publishMessagePool = new PublishMessagePool(
				publishMessageBufferCount / 2);
		this.confirmMessagePool = new PublishConfirmMessagePool(
				confirmMessageBufferCount / 2);
		// buffer
		this.publishBufferQueue = new ConcurrentLinkedQueue<ByteBuffer>();
		this.confirmBufferQueue = new ConcurrentLinkedQueue<ByteBuffer>();

		// TODO:improve buffer more efficient?
		this.publishBuffer = new byte[this.publishMessageSize
				* publishMessageBufferCount];
		this.confirmBuffer = new byte[this.confirmMessageSize
				* confirmMessageBufferCount];
		// fill message-buffer queue
		this.fillBufferQueue(this.publishBufferQueue, this.publishBuffer,
				publishMessageSize, publishMessageBufferCount);
		this.fillBufferQueue(this.confirmBufferQueue, this.confirmBuffer,
				confirmMessageSize, confirmMessageBufferCount);
	}

	public ByteBuffer getPublishBuffer(int length)
			throws messageTooLongException {
		if (length > this.publishMessageSize)
			throw new messageTooLongException();
		ByteBuffer buffer = this.publishBufferQueue.poll();
		if (buffer != null)
			buffer.position(0);
		return buffer;
	}

	public ByteBuffer getConfirmBuffer(int length)
			throws messageTooLongException {
		if (length > this.confirmMessageSize)
			throw new messageTooLongException();
		ByteBuffer buffer = this.confirmBufferQueue.poll();
		if (buffer != null)
			buffer.position(0);
		return buffer;
	}

	public PublishMessage acquirePublishMessage() {
		return this.publishMessagePool.acquire();
	}

	public PublishConfirmMessage acquireConfirmMessage() {
		return this.confirmMessagePool.acquire();
	}

	public synchronized void release(PublishMessage msg) {
		// return buffer for reusing
		if (msg.body != null && msg.body instanceof ByteBuffer) {
			this.publishBufferQueue.add((ByteBuffer) msg.body);
		}
		msg.clear();
		this.publishMessagePool.release(msg);
	}

	public synchronized void release(PublishConfirmMessage msg) {
		// return buffer for reusing
		if (msg.body != null && msg.body instanceof ByteBuffer) {
			this.confirmBufferQueue.add((ByteBuffer) msg.body);
		}
		msg.clear();
		this.confirmMessagePool.release(msg);
	}

	public Message parseMessage(String protocol, byte[] message, int offset,
			int length) throws messageTooLongException {
		// TODO:multi-protocol support
		// using our custom protocol currently
		int messageType = this.parseMessageType(message[offset]);

		Message msg = null;
		ByteBuffer buffer = null;

		if (messageType == MessageType.PUBLISH) {
			buffer = this.getPublishBuffer(length);
			msg = this.acquirePublishMessage();
		} else if (messageType == MessageType.PUBCONFIRM) {
			buffer = this.getConfirmBuffer(length);
			msg = this.acquireConfirmMessage();
		}

		if (msg == null) {
			System.out.println(String.format(
					"not support message: messageType=%s", messageType));
		} else if (buffer != null) {
			buffer.put(message, offset, length);
			msg = this.parse(msg, buffer);
		} else {
			System.out.println(String.format(
					"no buffer! drop message: messageType=%s", messageType));
		}
		return msg;
	}

	public byte[] parseMessage(String protocol, Message message) {
		// TODO:parse message to bytes
		return null;
	}

	private void fillBufferQueue(ConcurrentLinkedQueue<ByteBuffer> bufferQueue,
			byte[] buffer, int size, int count) {
		for (int i = 0; i < count; i++) {
			bufferQueue.add(ByteBuffer.wrap(buffer, i * size, size));
		}
	}

	// TODO: fill message from buffer by custom protocol
	private Message parse(Message msg, ByteBuffer buffer) {
		msg.messageType = 0;
		msg.messageSize = 1024;
		msg.to = "";
		msg.body = buffer;

		if (msg instanceof PublishMessage) {
			PublishMessage publishMessage = (PublishMessage) msg;
			publishMessage.id = "12345";
		} else if (msg instanceof PublishConfirmMessage) {
			PublishConfirmMessage confirmMessage = (PublishConfirmMessage) msg;
			confirmMessage.confirmId = "1,2,3";
		}

		return msg;
	}

	private int parseMessageType(byte headerByte) {
		return (headerByte & 240) >> 4;
	}
}
