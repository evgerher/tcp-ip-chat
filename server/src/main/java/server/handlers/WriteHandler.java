package server.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Server;

public class WriteHandler implements CompletionHandler<Integer, String> {
  private static final Logger logger = LoggerFactory.getLogger(ReadHandler.class);

  private final Server server;
  private final AsynchronousSocketChannel socket;

  public WriteHandler(Server server, AsynchronousSocketChannel socket) {
    this.server = server;
    this.socket = socket;
  }

  @Override
  public void completed(Integer result, String attachment) {
//    attachment.flip();
//    server.sendMessages(attachment);
//    attachment.clear();
//
//    ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
//    socket.read(bf, bf, new ReadHandler(server, socket));
    logger.info("Successfully sent message to {}", attachment);
  }

  @Override
  public void failed(Throwable exc, String attachment) {
    logger.error("Error during writing handling");
  }
}
