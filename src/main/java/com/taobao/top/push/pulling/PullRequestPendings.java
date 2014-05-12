package com.taobao.top.push.pulling;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PullRequestPendings{
	private Map<Object, AtomicBoolean> pendings;

	public PullRequestPendings() {
		this.pendings =  new TreeMap<Object, AtomicBoolean>();
	}

	public boolean setPending(Object request) {
		AtomicBoolean isPending = this.pendings.get(request);
		if (isPending == null) {
			synchronized (this.pendings) {
				if (this.pendings.get(request) == null)
					this.pendings.put(request, isPending = new AtomicBoolean());
			}
		}
		if (isPending.get())
			return false;
		synchronized (isPending) {
			if (isPending.get())
				return false;
			isPending.set(true);
			return true;
		}
	}

	public void cancelPending(Object request) {
		this.pendings.get(request).set(false);
	}
}
