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
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.Packet;

public class Server {
  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private final AsynchronousServerSocketChannel listener;
  private final List<AsynchronousSocketChannel> connections = Collections
      .synchronizedList(new ArrayList<AsynchronousSocketChannel>());

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

  private ByteBuffer sendMessages(ByteBuffer bf) {
    for (AsynchronousSocketChannel ch: connections)
      ch.write(bf);
    return bf;
  }

  public void runServer() throws ExecutionException, InterruptedException, IOException {
    while (true) {
      for (AsynchronousSocketChannel ch: connections) {
        CompletableFuture<ByteBuffer> readResult = CompletableFuture.supplyAsync(
            new Supplier<ByteBuffer>() {
              @Override
              public ByteBuffer get() {
                ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
                try {
                  ch.read(bf).get();
                } catch (InterruptedException e) {
                  logger.error(e.toString());
                } catch (ExecutionException p) {
                  logger.error(p.toString());
                }

                return bf;
              }
            }
        ).thenApplyAsync(new Function<ByteBuffer, ByteBuffer>() {
          @Override
          public ByteBuffer apply(ByteBuffer byteBuffer) {
            return sendMessages(byteBuffer);
          }
        });

        readResult.get();
      }
    }
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
  }

  public void removeClient(AsynchronousSocketChannel cl) {
    connections.remove(cl);
  }
}
