package com.taobao.top.mix;

import com.taobao.top.link.DefaultLogger;

public class ServerLogger extends DefaultLogger implements com.taobao.top.link.Logger, com.taobao.top.push.Logger {
	public ServerLogger(String type, 
			boolean isDebugEnabled, 
			boolean isInfoEnabled, 
			boolean isWarnEnabled, 
			boolean isErrorEnabled, 
			boolean isFatalEnabled) {
		super(type, isDebugEnabled, isInfoEnabled, isWarnEnabled, isErrorEnabled, isFatalEnabled);
	}
}
