package server.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Server;

public class WriteHandler implements CompletionHandler<Integer, ByteBuffer> {
  private static final Logger logger = LoggerFactory.getLogger(ReadHandler.class);

  private final Server server;
  private final AsynchronousSocketChannel socket;

  public WriteHandler(Server server, AsynchronousSocketChannel socket) {
    this.server = server;
    this.socket = socket;
  }

  @Override
  public void completed(Integer result, ByteBuffer attachment) {
//    attachment.flip();
//    server.sendMessages(attachment);
//    attachment.clear();
//
//    ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
//    socket.read(bf, bf, new ReadHandler(server, socket));
    logger.info("Successfully sent message to {}", socket);
  }

  @Override
  public void failed(Throwable exc, ByteBuffer attachment) {
    logger.error("Error during writing handling");
  }
}
