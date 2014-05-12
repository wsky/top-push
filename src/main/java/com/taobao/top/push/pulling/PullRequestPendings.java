package com.taobao.top.push.pulling;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PullRequestPendings {
	private Map<Object, Entry<AtomicBoolean, Long>> pendings;
	private int timeout = 1000 * 10;

	public PullRequestPendings() {
		this.pendings = new TreeMap<Object, Map.Entry<AtomicBoolean, Long>>();
	}

	public void setTimeout(int value) {
		this.timeout = value;
	}
	
	public void clear() {
		this.pendings.clear();
	}

	public boolean setPending(Object request) {
		Entry<AtomicBoolean, Long> p = this.pendings.get(request);

		if (p == null) {
			synchronized (this.pendings) {
				if (this.pendings.get(request) == null)
					this.pendings.put(request, p = this.createPending());
			}
		}

		if (this.isPending(p))
			return false;

		synchronized (p) {
			if (this.isPending(p))
				return false;
			p.getKey().set(true);
			p.setValue(System.currentTimeMillis());
			return true;
		}
	}

	public void cancelPending(Object request) {
		this.pendings.get(request).getKey().set(false);
	}

	private boolean isPending(Entry<AtomicBoolean, Long> e) {
		return e.getKey().get() && System.currentTimeMillis() - e.getValue() < this.timeout;
	}

	private Entry<AtomicBoolean, Long> createPending() {
		return new AbstractMap.SimpleEntry<AtomicBoolean, Long>(new AtomicBoolean(), System.currentTimeMillis());
	}
}
