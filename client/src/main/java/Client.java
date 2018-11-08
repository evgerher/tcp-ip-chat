import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  AsynchronousSocketChannel channel;

  public Client() throws Exception {
    logger.info("Client starts to establish connection");
    channel = AsynchronousSocketChannel.open();
    Future<Void> future = channel.connect(new InetSocketAddress("127.0.0.1", 10000));
    future.get();
    logger.info("Client connected");
  }

  public String msg(String val) throws Exception {
    byte[] byteMsg = new String(val).getBytes();
    ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
    Future<Integer> writeResult  = channel.write(buffer);
    for (int i = 0; i < 5; i++) {
      logger.info("Hello {}", i);
    }

    logger.info("write result code [{}]", writeResult.get());

    buffer.flip();
    Future<Integer> readResult  = channel.read(buffer);
    logger.info("read result code [{}]", readResult.get());

    String echo = new String(buffer.array()).trim();

    buffer.clear();
    return echo;
  }

  public void close() throws IOException {
    channel.close();
  }


  public static void main(String[] args) throws Exception {
    Client cl = new Client();
    String msg[] = new String[]{"Dog", "likes", "bob"};
    for (String s: msg)
      logger.info(cl.msg(s));
    cl.close();
  }
}
