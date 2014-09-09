package top.push;

public interface MessagingHandler {
	public boolean preSend();

	public void postSend(MessagingStatus status);

	public void exceptionCaught(Exception exception);
}
