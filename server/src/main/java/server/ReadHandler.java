package server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.Packet;

public class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {
  private static final Logger logger = LoggerFactory.getLogger(ReadHandler.class);

  private final Server server;
  private final AsynchronousSocketChannel socket;

  public ReadHandler(Server server, AsynchronousSocketChannel socket) {
    this.server = server;
    this.socket = socket;
  }

  @Override
  public void completed(Integer bytesRead, ByteBuffer attachment) {
//    attachment.flip();
//    attachment.clear();
    byte[] buffer = new byte[bytesRead];
    attachment.rewind();
    attachment.get(buffer);
    attachment.clear();

    ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
    socket.read(bf, bf, this);

    server.sendMessages(attachment, socket);
  }

  @Override
  public void failed(Throwable exc, ByteBuffer attachment) {
    logger.error("Error during reading handling");
  }
}
