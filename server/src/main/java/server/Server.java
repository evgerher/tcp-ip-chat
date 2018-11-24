package server;

import static java.util.concurrent.CompletableFuture.supplyAsync;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.Packet;
import server.handlers.AcceptHandler;
import server.handlers.ReadHandler;
import server.handlers.WriteHandler;

public class Server {
  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private final ExecutorService threadPool;
  private final AsynchronousServerSocketChannel listener;
  private final List<AsynchronousSocketChannel> connections = Collections
      .synchronizedList(new ArrayList<AsynchronousSocketChannel>());

  public Server() throws IOException, InterruptedException, ExecutionException {
    threadPool = Executors.newFixedThreadPool(10);
    AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(threadPool);
    logger.info("Start initialization of a server");

    listener = AsynchronousServerSocketChannel.open(group);
    InetSocketAddress address = new InetSocketAddress("localhost", 10001);
    listener.bind(address);

    AcceptHandler acceptCompletionHandler = new AcceptHandler(listener, this);
    listener.accept(null, acceptCompletionHandler);

    logger.info("End initialization of a server");
  }

  public void sendMessages(ByteBuffer bf, AsynchronousSocketChannel source) {
    synchronized (connections) {
      for (AsynchronousSocketChannel con : connections) {
        if (con != source) {
          ByteBuffer b = ByteBuffer.wrap(bf.array()); // Better to use copy, because some magic happens and ByteBuffer object contains wrong data
          logger.info("Write to socket :: user{}, content :: {}", connections.indexOf(con), new String(b.array()));
          con.write(b, String.format("user%d", connections.indexOf(con)), new WriteHandler(this, con));
        }
      }
    }
  }

  public static void main(String args[]) throws Exception {
    Server s = new Server();
    Thread.currentThread().join();
  }

  public void addClient(AsynchronousSocketChannel cl) {
    connections.add(cl);
    ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
    ReadHandler rh = new ReadHandler(this, cl);
    cl.read(bf, bf, rh);
  }

  public void removeClient(AsynchronousSocketChannel cl) {
    connections.remove(cl);
  }
}
