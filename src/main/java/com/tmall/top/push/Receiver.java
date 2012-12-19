package com.tmall.top.push;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.tmall.top.push.messages.PublishConfirmMessage;
import com.tmall.top.push.messages.PublishMessage;

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

	public ByteBuffer getPublishBuffer(int length) throws Exception {
		if (length > this.publishMessageSize)
			throw new Exception("length is bigger than max publishMessageSize");
		ByteBuffer buffer = this.publishBufferQueue.poll();
		if (buffer != null)
			buffer.position(0);
		return buffer;
	}

	public ByteBuffer getConfirmBuffer(int length) throws Exception {
		if (length > this.confirmMessageSize)
			throw new Exception("length is bigger than max confirmMessageSize");
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

	// public void receive(PublishMessage value) {
	// this.publishMessageQueue.add(value);
	// }
	//
	// public void receive(PublishConfirmMessage value) {
	// this.confirmMessageQueue.add(value);
	// }
	//
	// public PublishMessage pollPublishMessage() {
	// return this.publishMessageQueue.poll();
	// }
	//
	// public PublishConfirmMessage pollConfirmMessage() {
	// return this.confirmMessageQueue.poll();
	// }

	private void fillBufferQueue(ConcurrentLinkedQueue<ByteBuffer> bufferQueue,
			byte[] buffer, int size, int count) {
		for (int i = 0; i < count; i++) {
			bufferQueue.add(ByteBuffer.wrap(buffer, i * size, size));
		}
	}
}
