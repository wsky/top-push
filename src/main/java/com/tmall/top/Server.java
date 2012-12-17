package com.tmall.top;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class Server implements Runnable {
	
	private ByteBuffer _buffer;
	private Selector _selector;
	
	public void run() {
		
		this._buffer = ByteBuffer.allocate(1024);
		
		Selector acceptor = null;
		ServerSocketChannel serverChannel=null;
		
		try {
			serverChannel = ServerSocketChannel.open();
			serverChannel.socket().bind(new InetSocketAddress(8080));
			serverChannel.configureBlocking(false);
			
			this._selector = Selector.open();
			
			acceptor = Selector.open();
			serverChannel.register(acceptor, SelectionKey.OP_ACCEPT);
			
			while (true) {
				Iterator<SelectionKey> iterator = acceptor.selectedKeys().iterator();
				
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					this.processSelectKey(key);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			//throw new RuntimeException("Server failure: " + e.getMessage(), e);
		} finally {
			try {
				if(acceptor!=null) {
					acceptor.close();
				}
				if(serverChannel != null) {
					serverChannel.socket().close();
					serverChannel.close();
				}
			} catch (Exception e2) {
			
			}
		}
	}
	
	private void processSelectKey(SelectionKey key) throws IOException {
		if(key.isAcceptable()) {
			SocketChannel ch = ((ServerSocketChannel) key.channel()).accept();
			ch.configureBlocking(false);
			ch.register(this._selector, SelectionKey.OP_READ);
		} else if(key.isReadable()) {
			SocketChannel channel = (SocketChannel) key.channel();
            int count = channel.read(this._buffer);
            
            if (count > 0) {
            	this._buffer.flip();
            	CharBuffer charBuffer =Charset.forName("ASCII").decode(this._buffer);
            	System.out.println("received: " + charBuffer.toString());
            	channel.register(this._selector, SelectionKey.OP_WRITE).attach(new Handle());
            } else {
              channel.close();
            }
            
            this._buffer.clear();
            
		} else if(key.isWritable()) {
			SocketChannel channel = (SocketChannel) key.channel();
            Handle handle = (Handle) key.attachment();
            //ByteBuffer block = ByteBuffer.wrap(handle.readBlock().getBytes());
            //channel.write(block);
		}
	}
	
	
	
	private void beginAccept(Selector acceptor) {
		
	}
	private void processAccept() {		
	}
}

class Handle {
	
	
}
