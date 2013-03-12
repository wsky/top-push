package com.taobao.top.push.messages;

@Deprecated
public class PublishConfirmMessage extends Message {
	// support batch
	// "1,2,3"
	public String confirmId;

	public PublishConfirmMessage() {
		this.messageType = MessageType.PUBCONFIRM;
	}

	// @Override
	// protected void internalClear() {
	// this.messageType = MessageType.PUBCONFIRM;
	// this.confirmId = null;
	// }
}
