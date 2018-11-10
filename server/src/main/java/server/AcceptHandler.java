package server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {
  private static final Logger logger = LoggerFactory.getLogger(AcceptHandler.class);

  private AsynchronousServerSocketChannel listener;
  private Server server;

  public AcceptHandler(AsynchronousServerSocketChannel listener, Server server) {
    this.listener = listener;
    this.server = server;
  }

  public void completed(AsynchronousSocketChannel socketChannel, Void arg1) {
    logger.info("client connected: {}", socketChannel);
    listener.accept(null, this);
  }

  public void failed(Throwable arg0, Void arg1) {
  }
}
