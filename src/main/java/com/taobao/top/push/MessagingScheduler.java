package com.taobao.top.push;

public interface MessagingScheduler {
	public void schedule(Client client, MessagingTask messaging);
}
