package com.taobao.top.push.messages;

@Deprecated
public class PublishMessage extends Message {
	public String id;

	public PublishMessage() {
		this.messageType = MessageType.PUBLISH;
	}

	// @Override
	// protected void internalClear() {
	// this.messageType = MessageType.PUBLISH;
	// this.id = null;
	// }
}
