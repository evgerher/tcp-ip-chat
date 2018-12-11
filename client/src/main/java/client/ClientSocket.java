package client;

import client.handlers.MessageProcessor;
import client.handlers.ReadHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.*;

public class ClientSocket extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(ClientSocket.class);
  private Future<Void> connection;

  private final MessageProcessor messageProcessor;
  private AsynchronousSocketChannel channel;
  private boolean closed;

  public ClientSocket(MessageProcessor mprocessor) throws IOException, InterruptedException, ExecutionException {
    this.messageProcessor = mprocessor;
    closed = false;

    logger.info("Client starts to establish connection");
    channel = AsynchronousSocketChannel.open();
    connection = channel.connect(new InetSocketAddress("127.0.0.1", 10001));
    connection.get();
    logger.info("Client connected");
  }

  public void sendMessage(Message msg) throws ExecutionException, InterruptedException {
    Packet[] packets = msg.getPackets();

    for (Packet p: packets) {
      ByteBuffer bf = p.byteBuffer();
      Future<Integer> writeResult  = channel.write(bf); // todo: refactor to async call
      logger.info("write result code [{}]", writeResult.get());
    }
  }

  public void close() throws IOException {
    channel.close();
    closed = true;
  }

  @Override
  public void run() {
    ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
    MessageBuilder builder = new MessageBuilder(messageProcessor);
    channel.read(bf, bf, new ReadHandler(channel, builder));
    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      logger.error(e.toString());
    }
  }
}
