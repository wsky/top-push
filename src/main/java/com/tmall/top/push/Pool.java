package com.tmall.top.push;

public abstract class Pool<T> {
	//private final int DefaultPoolSize = 10;
	private T[] items;
	private int count;
	private int poolSize;

	@SuppressWarnings("unchecked")
	public Pool(int poolSize) {
		this.items = (T[]) new Object[poolSize];
		this.poolSize = poolSize;
	}

	public synchronized T acquire() {
		if (this.count > 0) {
			this.count--;
			T item = this.items[this.count];
			return item;
		} else {
			return this.createNew();
		}
	}

	public synchronized void release(T item) {
		if (this.count < this.poolSize) {
			this.items[this.count] = item;
			this.count++;
		}
	}

	public abstract T createNew();
}
