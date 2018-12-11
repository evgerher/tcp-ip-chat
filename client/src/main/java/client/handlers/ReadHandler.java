package client.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.MessageBuilder;
import packets.Packet;

public class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {
  private static final Logger logger = LoggerFactory.getLogger(ReadHandler.class);

  private final MessageBuilder builder;
  private final AsynchronousSocketChannel socket;

  public ReadHandler(AsynchronousSocketChannel socket, MessageBuilder builder) {
    this.socket = socket;
    this.builder = builder;
  }

  @Override
  public void completed(Integer bytesRead, ByteBuffer attachment) {
//    attachment.clear();

    ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
    socket.read(bf, bf, this);

    attachment.rewind();
    builder.acceptPacket(attachment);
  }

  @Override
  public void failed(Throwable exc, ByteBuffer attachment) {
    logger.error("Error during reading handling, {}", exc.toString());
  }
}
