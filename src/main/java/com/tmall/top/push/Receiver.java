package com.tmall.top.push;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.tmall.top.push.messaging.PublishConfirmMessage;
import com.tmall.top.push.messaging.PublishMessage;

public class Receiver {
	private final static int POOL_SIZE = 10;
	// private Object publishLock = new String("publishLock");
	// private Object confirmLock = new String("confirmLock");

	private PublishMessagePool publishMessagePool;
	private PublishConfirmMessagePool confirmMessagePool;
	private byte[] publishBuffer;
	private byte[] confirmBuffer;
	private ConcurrentLinkedQueue<ByteBuffer> publishBufferQueue;
	private ConcurrentLinkedQueue<ByteBuffer> confirmBufferQueue;
	private ConcurrentLinkedQueue<PublishMessage> publishMessageQueue;
	private ConcurrentLinkedQueue<PublishConfirmMessage> confirmMessageQueue;

	public Receiver(int publishMessageSize, int confirmMessageSize,
			int publishMessageBufferCount, int confirmMessageBufferCount) {
		// object pool
		this.publishMessagePool = new PublishMessagePool(POOL_SIZE);
		this.confirmMessagePool = new PublishConfirmMessagePool(POOL_SIZE);
		// buffer
		this.publishBufferQueue = new ConcurrentLinkedQueue<ByteBuffer>();
		this.confirmBufferQueue = new ConcurrentLinkedQueue<ByteBuffer>();
		// queue
		this.publishMessageQueue = new ConcurrentLinkedQueue<PublishMessage>();
		this.confirmMessageQueue = new ConcurrentLinkedQueue<PublishConfirmMessage>();

		// TODO:improve buffer more efficient
		this.publishBuffer = new byte[publishMessageSize
				* publishMessageBufferCount];
		this.confirmBuffer = new byte[confirmMessageSize
				* confirmMessageBufferCount];

		this.fillBufferQueue(this.publishBufferQueue, this.publishBuffer,
				publishMessageSize, publishMessageBufferCount);
		this.fillBufferQueue(this.confirmBufferQueue, this.confirmBuffer,
				confirmMessageSize, confirmMessageBufferCount);
	}

	public ByteBuffer getPublishBuffer() {
		ByteBuffer buffer = this.publishBufferQueue.poll();
		if (buffer != null)
			buffer.position(0);
		return buffer;
	}

	public ByteBuffer getConfirmBuffer() {
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
		// return buffer
		if (msg.body instanceof ByteBuffer) {
			this.publishBufferQueue.add((ByteBuffer) msg.body);
		}
		msg.clear();
		this.publishMessagePool.release(msg);
	}

	public synchronized void release(PublishConfirmMessage msg) {
		// return buffer
		if (msg.body instanceof ByteBuffer) {
			this.confirmBufferQueue.add((ByteBuffer) msg.body);
		}
		msg.clear();
		this.confirmMessagePool.release(msg);
	}

	public void receive(PublishMessage value) {
		this.publishMessageQueue.add(value);
	}

	public void receive(PublishConfirmMessage value) {
		this.confirmMessageQueue.add(value);
	}

	private void fillBufferQueue(ConcurrentLinkedQueue<ByteBuffer> bufferQueue,
			byte[] buffer, int size, int count) {
		for (int i = 0; i < count; i++) {
			bufferQueue.add(ByteBuffer.wrap(buffer, i * size, size));
		}
	}
}
