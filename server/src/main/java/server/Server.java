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
import packets.Message;
import packets.MessageAcceptor;
import packets.MessageBuilder;
import packets.Packet;
import server.handlers.AcceptHandler;
import server.handlers.ReadHandler;
import server.handlers.WriteHandler;

public class Server implements MessageAcceptor<AsynchronousSocketChannel> {
  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private final int INITIAL_ROOM = 0;
  private final AsynchronousServerSocketChannel listener;
  private final List<AsynchronousSocketChannel> connections = Collections
      .synchronizedList(new ArrayList<>());
  private final Map<Integer, List<AsynchronousSocketChannel>> rooms;
  private final MessageBuilder messageBuilder;

  private AsynchronousSocketChannel annotation;

  public Server() throws IOException, InterruptedException, ExecutionException {
    logger.info("Start initialization of a server");
    ExecutorService threadPool = Executors.newFixedThreadPool(10); //TODO: what is it for?
    AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(threadPool);
    messageBuilder = new MessageBuilder(this);
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

  private void addClientToRoom(AsynchronousSocketChannel client, Integer room) {
    synchronized (rooms) { //todo: should I?
      try {
        rooms.get(room).add(client);
      } catch (RuntimeException e) {
        logger.error("No room by requested id");
      }
    }
  }

  public void  processPacket(ByteBuffer bf, AsynchronousSocketChannel source) {
    ByteBuffer b = ByteBuffer.wrap(bf.array());
    int room = b.getInt();
    boolean isCommand = b.getInt() > 0;
    b.rewind();

    if (isCommand) {
      messageBuilder.acceptPacket(b);
    } else {
      sendPacket(b, source, room);
    }
//    synchronized (connections) {
//      for (AsynchronousSocketChannel con : connections) {
//        if (con != source) {
//          ByteBuffer b = ByteBuffer.wrap(bf.array()); // Better to use copy, because some magic happens and ByteBuffer object contains wrong data
//          logger.debug("Write to socket :: user{}, content :: {}", connections.indexOf(con), new String(b.array()));
//          con.write(b, String.format("user%d", connections.indexOf(con)), new WriteHandler(this, con));
//        }
//      }
//    }
  }

  private void processCommand(Message message) {
    String cmd = new String(message.getContent());
    if (cmd.startsWith("/register")) {
      int len = "/register".length();
      String substring = cmd.substring(len);
      try {
        Integer id = Integer.parseInt(substring);
        synchronized (rooms) { // todo: should I???
          List<AsynchronousSocketChannel> list = rooms.put(id, Collections.synchronizedList(new ArrayList<>()));
          list.add(getAnnotation());
        }
      } catch (RuntimeException e) {
        e.printStackTrace();
        logger.error("Register :: Could not parse integer for string [{}]", cmd); // todo: better message for logger
      }
    } else if (cmd.startsWith("/connect")) {
      int len = "/connect".length();
      String substring = cmd.substring(len);
      try {
        Integer id = Integer.parseInt(substring);
        synchronized (rooms) { // todo: should I???
          List<AsynchronousSocketChannel> list = rooms.getOrDefault(id, null);
          if (list != null)
            list.add(getAnnotation());
          else {
            //todo: send message back !
            logger.error("Room {} does not exist", id);
          }
        }
      } catch (RuntimeException e) {
        e.printStackTrace();
        logger.error("Connect :: Could not parse integer for string [{}]", cmd); // todo: better message for logger
      }
    }
  }

  private void sendPacket(ByteBuffer b, AsynchronousSocketChannel source, int room) {

    List<AsynchronousSocketChannel> roomMembers;
    synchronized (rooms) {
      roomMembers = rooms.get(room); //TODO: IS IT LEGAL ???
    }

    synchronized (roomMembers) { //TODO: IS IT LEGAL ???
      for (AsynchronousSocketChannel con : roomMembers) {
        if (con != source) {
          con.write(b, String.format("user%d room%d", connections.indexOf(con), room),
              new WriteHandler(this, con));
          logger.debug("Write to socket :: user{}, room :: {}", connections.indexOf(con), room);
        }
      }
    }
  }

  public void addClient(AsynchronousSocketChannel cl) {
    synchronized (connections) { //todo: should I?
      connections.add(cl);
    }
    addClientToRoom(cl, INITIAL_ROOM);

    ByteBuffer bf = ByteBuffer.allocate(Packet.PACKET_SIZE);
    ReadHandler rh = new ReadHandler(this, cl);
    cl.read(bf, bf, rh);
  }

  public void removeClient(AsynchronousSocketChannel cl) {
    connections.remove(cl);
  }

  public static void main(String args[]) throws Exception {
    Server s = new Server();
    Thread.currentThread().join();
  }

  @Override
  public void acceptMessage(Message message) {
    //todo: me
    if (message.isCommand())
      processCommand(message);
  }

  @Override
  public void setAnnotation(AsynchronousSocketChannel annotation) {
    this.annotation = annotation;
  }

  @Override
  public AsynchronousSocketChannel getAnnotation() {
    return annotation;
  }
}
