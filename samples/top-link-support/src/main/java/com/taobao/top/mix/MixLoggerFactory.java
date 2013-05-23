package com.taobao.top.mix;

import com.taobao.top.link.DefaultLoggerFactory;

public class MixLoggerFactory extends DefaultLoggerFactory
		implements com.taobao.top.push.LoggerFactory, com.taobao.top.link.LoggerFactory {

	private boolean isDebugEnabled;
	private boolean isInfoEnabled;
	private boolean isWarnEnabled;
	private boolean isErrorEnabled;
	private boolean isFatalEnabled;

	public MixLoggerFactory() {
		this(false, true, true, true, true);
	}

	public MixLoggerFactory(boolean isDebugEnabled,
			boolean isInfoEnabled,
			boolean isWarnEnabled,
			boolean isErrorEnabled,
			boolean isFatalEnabled) {
		this.isDebugEnabled = isDebugEnabled;
		this.isInfoEnabled = isInfoEnabled;
		this.isWarnEnabled = isWarnEnabled;
		this.isErrorEnabled = isErrorEnabled;
		this.isFatalEnabled = isFatalEnabled;
	}

	@Override
	public ServerLogger create(String type) {
		return new ServerLogger(type,
				this.isDebugEnabled,
				this.isInfoEnabled,
				this.isWarnEnabled,
				this.isErrorEnabled,
				this.isFatalEnabled);
	}

	@Override
	public ServerLogger create(Class<?> type) {
		return new ServerLogger(type.getSimpleName(),
				this.isDebugEnabled,
				this.isInfoEnabled,
				this.isWarnEnabled,
				this.isErrorEnabled,
				this.isFatalEnabled);
	}

	@Override
	public ServerLogger create(Object object) {
		return new ServerLogger(object.getClass().getSimpleName(),
				this.isDebugEnabled,
				this.isInfoEnabled,
				this.isWarnEnabled,
				this.isErrorEnabled,
				this.isFatalEnabled);
	}
}
