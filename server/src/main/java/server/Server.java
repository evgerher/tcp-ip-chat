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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.Packet;

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

  public ByteBuffer sendMessages(ByteBuffer bf, AsynchronousSocketChannel source) {
    for (AsynchronousSocketChannel con: connections) {
      if (con != source) {
        logger.info("Write to socket :: user{}", connections.indexOf(con));
        con.write(bf, bf, new WriteHandler(this, con));
        bf.rewind();
      }
    }

    bf.clear();
    return bf;
  }

  protected CompletableFuture<ByteBuffer> buildFuture(AsynchronousSocketChannel ch) {
    CompletableFuture future = CompletableFuture.supplyAsync(() -> {
      logger.info("Build future ::  user{}", connections.indexOf(ch));
      ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
      try {
        ch.read(bf).get();
      } catch (InterruptedException e) {
        logger.error(e.toString());
      } catch (ExecutionException e) {
        logger.error(e.toString());
      }

      bf.flip();
      logger.info("Read message :: {}", new String(bf.array()));
      return bf;
    }).thenApplyAsync(new Function<ByteBuffer, ByteBuffer>() {
      @Override
      public ByteBuffer apply(ByteBuffer byteBuffer) {
        logger.info("Apply method :: {}", new String(byteBuffer.array()));
//        sendMessages(byteBuffer);
        try {
          return buildFuture(ch).get();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }

        return null;
      }
    });

    return future;
  }

  public void runServer() throws ExecutionException, InterruptedException, IOException {
//    for (AsynchronousSocketChannel ch: connections) {
//      CompletableFuture<ByteBuffer> readResult = buildFuture(ch);
//      readResult.get();
//    }

//    if ((worker != null) && (worker.isOpen())) {
//      while (true) {
//        if (!worker.isOpen())
//          break;
//        ByteBuffer buffer = ByteBuffer.allocate(Packet.PACKET_SIZE);
//        Future<Integer> readResult  = worker.read(buffer);
//        // perform other computations
//
//        readResult.get();
//        logger.trace("Read data from user [{}]", new String(buffer.array()));
//
//        buffer.flip();
//        Future<Integer> writeResult = worker.write(buffer);
//
//        // perform other computations
//
//        Integer res = writeResult.get();
//        logger.trace("Sent data to user [{}]", new String(buffer.array()));
//        buffer.clear();
//      }
//      worker.close();
//      listener.close();
//    }
  }

  public static void main(String args[]) throws Exception {
    Server s = new Server();
    s.runServer();
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
