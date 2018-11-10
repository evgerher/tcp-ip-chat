package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.Packet;

public class Server {
  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private final AsynchronousServerSocketChannel listener;
  private final List<AsynchronousSocketChannel> connections = Collections
      .synchronizedList(new ArrayList<AsynchronousSocketChannel>());
  private AsynchronousSocketChannel worker;

  public Server() throws IOException, InterruptedException, ExecutionException {
    ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(10);
    AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(newFixedThreadPool);


    logger.info("Start initialization of a server");
    listener = AsynchronousServerSocketChannel.open(group);
    InetSocketAddress address = new InetSocketAddress("localhost", 10001);
    listener.bind(address);
    AcceptHandler acceptCompletionHandler = new AcceptHandler(listener, this);
    listener.accept(null, acceptCompletionHandler);
    logger.info("End initialization of a server");
  }

  public void runServer() throws ExecutionException, InterruptedException, IOException {
    if ((worker != null) && (worker.isOpen())) {
      while (true) {
        if (!worker.isOpen())
          break;
        ByteBuffer buffer = ByteBuffer.allocate(Packet.PACKET_SIZE);
        Future<Integer> readResult  = worker.read(buffer);
        // perform other computations

        readResult.get();
        logger.trace("Read data from user [{}]", new String(buffer.array()));

        buffer.flip();
        Future<Integer> writeResult = worker.write(buffer);

        // perform other computations

        Integer res = writeResult.get();
        logger.trace("Sent data to user [{}]", new String(buffer.array()));
        buffer.clear();
      }
      worker.close();
      listener.close();
    }
  }

  public static void main(String args[]) throws Exception {
    Server s = new Server();
    s.runServer();
  }
}
