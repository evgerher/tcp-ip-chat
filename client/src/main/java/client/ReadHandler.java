package client;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.MessageBuilder;
import packets.Packet;

public class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {
  private static final Logger logger = LoggerFactory.getLogger(ReadHandler.class);

  private final Client client;
  private final AsynchronousSocketChannel socket;
  private MessageBuilder builder;

  public ReadHandler(AsynchronousSocketChannel socket, Client client) {
    this.socket = socket;
    this.client = client;
    builder = new MessageBuilder();
  }

  @Override
  public void completed(Integer bytesRead, ByteBuffer attachment) {
//    attachment.clear();

    ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
    socket.read(bf, bf, this);

    attachment.rewind();
    builder.acceptBytes(attachment);

    if (builder.isConstructed()) {
      client.acceptMessage(builder.getMessage());
      builder = new MessageBuilder(); // todo: make somekind of reinitialization instead
    }
  }

  @Override
  public void failed(Throwable exc, ByteBuffer attachment) {
    logger.error("Error during reading handling");
  }
}
