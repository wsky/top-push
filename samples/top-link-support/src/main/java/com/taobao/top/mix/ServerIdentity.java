package com.taobao.top.mix;

import java.util.Map;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.endpoint.Identity;

public class ServerIdentity implements Identity {
	@SuppressWarnings("unchecked")
	@Override
	public Identity parse(Object data) throws LinkException {
		return new ClientIdentity(((Map<String, String>) data).get("appkey"));
	}

	@Override
	public void render(Object to) {
	}

	@Override
	public boolean equals(Identity id) {
		return id instanceof ServerIdentity;
	}

}
