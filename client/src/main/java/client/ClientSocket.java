package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.Message;
import packets.MessageBuilder;
import packets.Packet;

public class ClientSocket extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(ClientSocket.class);
  private Future<Void> connection;

  private final Client client;
  private AsynchronousSocketChannel channel;
  private boolean closed;

  public ClientSocket(Client client) throws IOException, InterruptedException, ExecutionException {
    this.client = client;
    closed = false;

    logger.info("client.Client starts to establish connection");
    channel = AsynchronousSocketChannel.open();
    connection = channel.connect(new InetSocketAddress("127.0.0.1", 10001));
    connection.get();
    logger.info("client.Client connected");
  }

  public void sendMessage(Message msg) throws ExecutionException, InterruptedException {
    Packet[] packets = msg.getPackets();

    for (Packet p: packets) {
      ByteBuffer bf = p.byteBuffer();
      Future<Integer> writeResult  = channel.write(bf);
//    for (int i = 0; i < 5; i++) {
//      logger.info("Hello {}", i);
//    }
      logger.info("write result code [{}]", writeResult.get());
    }
  }

  public void close() throws IOException {
    channel.close();
    closed = true;
  }


  @Override
  public void run() {
    try {
      ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
      MessageBuilder mbuilder = new MessageBuilder();
      while (!closed) {
        Future<Integer> readResult = channel.read(bf);
        int size = readResult.get();
        logger.info("read result code [{}]", size);

        bf.rewind();
        byte[] buf = new byte[size];
        bf.get(buf, 0, size);
        ByteBuffer local = ByteBuffer.wrap(buf);
        logger.info("BUFFER STUFF {}, {}", local.array(), local.limit());
        mbuilder.acceptBytes(local);
        if (mbuilder.isConstructed()) {
          client.acceptMessage(mbuilder.getMessage());
          mbuilder = new MessageBuilder();
        }
        bf.clear();
      }
    } catch (ExecutionException | InterruptedException e) {
      logger.error(e.toString());
    }
  }
}
