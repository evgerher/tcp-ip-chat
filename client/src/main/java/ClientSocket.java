import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSocket extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(ClientSocket.class);
  private ByteBuffer buffer = ByteBuffer.allocate(32);
  private Future<Void> connection;

  private final Client client;
  private AsynchronousSocketChannel channel;
  private boolean closed;

  public ClientSocket(Client client) throws IOException, InterruptedException, ExecutionException {
    this.client = client;
    closed = false;

    logger.info("Client starts to establish connection");
    channel = AsynchronousSocketChannel.open();
    connection = channel.connect(new InetSocketAddress("127.0.0.1", 10001));
  }

  public void sendMessage(String val) throws ExecutionException, InterruptedException {
    byte[] byteMsg = new String(val).getBytes();
    ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
    Future<Integer> writeResult  = channel.write(buffer);
//    for (int i = 0; i < 5; i++) {
//      logger.info("Hello {}", i);
//    }
    logger.info("write result code [{}]", writeResult.get());
    buffer.clear();
  }

  public void close() throws IOException {
    channel.close();
    closed = true;
  }


  @Override
  public void run() {
    try {
      connection.get();
      logger.info("Client connected");
      while (!closed) {
        Future<Integer> readResult = channel.read(buffer);
        logger.info("read result code [{}]", readResult.get());
        String echo = new String(buffer.array()).trim();
        client.acceptMessage(echo);
        buffer.clear();
      }
    } catch (ExecutionException | InterruptedException e) {
      logger.error(e.toString());
    }
  }
}
