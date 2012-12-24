package com.tmall.top.push;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.tmall.top.push.messages.Message;
import com.tmall.top.push.messages.MessageIO;
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

	// provide message parser, receiving-buffer and improvement
	public Receiver(int publishMessageSize, int confirmMessageSize,
			int publishMessageBufferCount, int confirmMessageBufferCount) {
		// message size
		this.publishMessageSize = publishMessageSize;
		this.confirmMessageSize = confirmMessageSize;
		// object pool
		// is it necessary ?
		this.publishMessagePool = new PublishMessagePool(
				publishMessageBufferCount / 2);
		this.confirmMessagePool = new PublishConfirmMessagePool(
				confirmMessageBufferCount / 2);
		// buffer
		this.publishBufferQueue = new ConcurrentLinkedQueue<ByteBuffer>();
		this.confirmBufferQueue = new ConcurrentLinkedQueue<ByteBuffer>();
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

	// must be called after send
	public synchronized void release(Message msg) {
		// return buffer for reusing
		if (msg.body != null && msg.body instanceof ByteBuffer) {
			this.publishBufferQueue.add((ByteBuffer) msg.body);
		}
		msg.clear();
		if (msg instanceof PublishMessage)
			this.publishMessagePool.release((PublishMessage) msg);
		if (msg instanceof PublishConfirmMessage)
			this.confirmMessagePool.release((PublishConfirmMessage) msg);
	}

	// for receiving message from lower buffer
	public Message parseMessage(String protocol, byte[] message, int offset,
			int length) throws MessageTooLongException,
			MessageTypeNotSupportException, NoMessageBufferException {
		// TODO:multi-protocol support, design parser factory
		// using our custom protocol currently
		int messageType = MessageIO.parseMessageType(message[offset]);

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
			throw new MessageTypeNotSupportException();
		} else if (buffer != null) {
			buffer.put(message, offset, length);
			msg = MessageIO.parseServerReceiving(msg, buffer);
		} else {
			throw new NoMessageBufferException();
		}
		return msg;
	}

	// for send message to lower buffer
	public ByteBuffer parseMessage(String protocol, Message message) {
		return MessageIO.parseServerSending(message, (ByteBuffer) message.body);
	}

	private PublishMessage acquirePublishMessage() {
		return this.publishMessagePool.acquire();
	}

	private PublishConfirmMessage acquireConfirmMessage() {
		return this.confirmMessagePool.acquire();
	}

	private ByteBuffer getPublishBuffer(int length)
			throws MessageTooLongException {
		if (length > this.publishMessageSize)
			throw new MessageTooLongException();
		// TODO: if no buffer, retry twice with lock-free?
		ByteBuffer buffer = this.publishBufferQueue.poll();
		if (buffer != null)
			buffer.position(0);
		return buffer;
	}

	private ByteBuffer getConfirmBuffer(int length)
			throws MessageTooLongException {
		if (length > this.confirmMessageSize)
			throw new MessageTooLongException();
		ByteBuffer buffer = this.confirmBufferQueue.poll();
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
}
