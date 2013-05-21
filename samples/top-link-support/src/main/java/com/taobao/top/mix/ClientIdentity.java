package com.taobao.top.mix;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.endpoint.Identity;

public class ClientIdentity implements com.taobao.top.push.Identity, com.taobao.top.link.endpoint.Identity {
	private String appKey;

	public ClientIdentity(String appKey) {
		this.appKey = appKey;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass().equals(ClientIdentity.class) &&
				this.appKey.equals(((ClientIdentity) obj).appKey);
	}

	@Override
	public String toString() {
		return this.appKey;
	}

	@Override
	public int hashCode() {
		return this.appKey.hashCode();
	}

	@Override
	public Identity parse(Object data) throws LinkException {
		return null;
	}

	@Override
	public void render(Object to) {
	}

	@Override
	public boolean equals(Identity id) {
		return this.equals((Object) id);
	}
}
