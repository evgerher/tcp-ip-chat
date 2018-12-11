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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
  public final static int INITIAL_ROOM = 0;

  private final AsynchronousServerSocketChannel listener;
  private final CommandProcessor cmdProcessor;
  private final List<AsynchronousSocketChannel> connections;
  private final Map<Integer, List<AsynchronousSocketChannel>> rooms;

  private volatile AsynchronousSocketChannel annotation;

  public Server() throws IOException {
    logger.info("Start initialization of a server");
    connections = Collections.synchronizedList(new ArrayList<>());
    ExecutorService threadPool = Executors.newFixedThreadPool(10); //TODO: what is it for?
    AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(threadPool);

    cmdProcessor = new CommandProcessor(this);
    rooms = initRooms();

    listener = AsynchronousServerSocketChannel.open(group);
    InetSocketAddress address = new InetSocketAddress("localhost", 10001);
    listener.bind(address);

    AcceptHandler acceptCompletionHandler = new AcceptHandler(listener, this);
    listener.accept(null, acceptCompletionHandler);

    logger.info("End initialization of a server");
  }

  private Map<Integer, List<AsynchronousSocketChannel>> initRooms() {
    Map<Integer, List<AsynchronousSocketChannel>> rooms = new ConcurrentHashMap<>(5);
    rooms.put(INITIAL_ROOM, Collections.synchronizedList(new ArrayList<>()));
    //todo: load rooms
    return rooms;
  }

  public void  processPacket(ByteBuffer bf, AsynchronousSocketChannel source) {
    int room = bf.getInt();
    boolean isCommand = bf.getInt() > 0;
    bf.rewind();

    if (isCommand) {
      cmdProcessor.process(bf, source);
    } else {
      sendPacket(bf, source, room);
    }
//    synchronized (connections) { // This code sends messages to everyone
//      for (AsynchronousSocketChannel con : connections) {
//        if (con != source) {
//          ByteBuffer b = ByteBuffer.wrap(bf.array()); // Better to use copy, because some magic happens and ByteBuffer object contains wrong data
//          logger.debug("Write to socket :: user{}, content :: {}", connections.indexOf(con), new String(b.array()));
//          con.write(b, String.format("user%d", connections.indexOf(con)), new WriteHandler(this, con));
//        }
//      }
//    }
  }

  private void sendPacket(ByteBuffer bf, AsynchronousSocketChannel source, int room) {
    List<AsynchronousSocketChannel> roomMembers;
    synchronized (rooms) {
      roomMembers = rooms.get(room); //TODO: IS IT LEGAL ???
    }

    synchronized (roomMembers) { //TODO: IS IT LEGAL ???
      for (AsynchronousSocketChannel con : roomMembers) {
        ByteBuffer b = bf.duplicate();
        if (con != source) {
          con.write(b, String.format("user%d room%d", connections.indexOf(con), room),
              new WriteHandler(this, con));
          logger.debug("Write to socket :: user{}, room :: {}", connections.indexOf(con), room);
        }
      }
    }
  }

  public void registerClient(AsynchronousSocketChannel cl) {
    synchronized (connections) { //todo: should I?
      connections.add(cl);
    }
    connectClientToRoom(cl, INITIAL_ROOM);

    ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
    ReadHandler rh = new ReadHandler(this, cl);
    cl.read(bf, bf, rh);
  }

  public void connectClientToRoom(AsynchronousSocketChannel cl, int room) {
    List<AsynchronousSocketChannel> list = rooms.getOrDefault(room, null);
    if (list != null) {
      list.add(cl);
      logger.info("Client {} connected to room {}", cl, room);
    }
    else {
      //todo: send message back !
      logger.error("Room {} does not exist", room);
    }
  }

  public void removeClient(AsynchronousSocketChannel cl) {
    connections.remove(cl);
  }

  public static void main(String args[]) throws Exception {
    Server s = new Server();
    Thread.currentThread().join();
  }

  public void registerRoom(Integer id) {
    synchronized (rooms) { // todo: should I???
      rooms.put(id, Collections.synchronizedList(new ArrayList<>()));
//          rooms.put(id, Collections.synchronizedList(new ArrayList<>());  // todo: why returns null if used new ArrayList() ???
      logger.info("Registered room {}", id);
    }
  }
}
