package com.taobao.top.mix;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.endpoint.Identity;

public class ServerIdentity implements Identity {
	@Override
	public Identity parse(Object data) throws LinkException {
		return null;
	}

	@Override
	public void render(Object to) {
	}

	@Override
	public boolean equals(Identity id) {
		return false;
	}

}
