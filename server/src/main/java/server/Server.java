package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private final AsynchronousServerSocketChannel server;
  private AsynchronousSocketChannel worker;

  public Server() throws IOException, InterruptedException, ExecutionException {
    logger.info("Start initialization of a server");
    server = AsynchronousServerSocketChannel.open();
    server.bind(new InetSocketAddress("127.0.0.1", 10001));
    logger.info("End initialization of a server");
    Future<AsynchronousSocketChannel> acceptFuture = server.accept();
    worker = acceptFuture.get();
    logger.info("Accepted connection");
  }

  public void runServer() throws ExecutionException, InterruptedException, IOException {
    if ((worker != null) && (worker.isOpen())) {
      while (true) {
        if (!worker.isOpen())
          break;
        ByteBuffer buffer = ByteBuffer.allocate(32);
        Future<Integer> readResult  = worker.read(buffer);
        // perform other computations

        readResult.get();
        logger.info("Read data from user [{}]", new String(buffer.array()));

        buffer.flip();
        Future<Integer> writeResult = worker.write(buffer);

        // perform other computations

        Integer res = writeResult.get();
        logger.info("Sent data to user [{}]", new String(buffer.array()));
        if (res == -1)
          break;
        buffer.clear();
      }
      worker.close();
      server.close();
    }
  }

  public static void main(String args[]) throws Exception {
    Server s = new Server();
    s.runServer();
  }
}
