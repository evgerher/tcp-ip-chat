package server.handlers;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Server;

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
    server.registerClient(socketChannel);
    logger.info("Successfully connected");
  }

  public void failed(Throwable arg0, Void arg1) {
    logger.error("Failed to establish connection, {}", arg0);
  }
}
