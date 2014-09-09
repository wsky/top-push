package top.push;

public abstract class MessagingTask implements Runnable {
	@Override
	public void run() {
		this.execute();
	}

	public abstract MessagingStatus execute();
}
