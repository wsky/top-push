package com.taobao.top.push;


public class DefaultIdentity {
	private String id;

	public DefaultIdentity(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass().equals(DefaultIdentity.class) &&
				this.id.equals(((DefaultIdentity) obj).id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	@Override
	public String toString() {
		return this.id;
	}
}