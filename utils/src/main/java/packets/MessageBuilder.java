package packets;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageBuilder {
  private Message message;
  private Map<Integer, List<Packet>> roomPackets;
  private final MessageAcceptor acceptor;

  public MessageBuilder(MessageAcceptor acceptor) {
    roomPackets = new ConcurrentHashMap<>();
    this.acceptor = acceptor;
  }

  public synchronized void acceptPacket(ByteBuffer bf) {  // Reimplement in order to work with different chats
    Packet packet = Packet.decode(bf);
    storePacket(packet);
    boolean last = packet.isLast();

    if (last) {
      int roomid = packet.getRoomid();
      message = buildMessage(roomPackets.get(roomid), roomid);
      acceptor.acceptMessage(message);
    }
  }

  private Message buildMessage(List<Packet> packetList, int roomid) {
    List<Packet> roomList = roomPackets.remove(roomid);
    Packet[] packets = new Packet[roomList.size()];
    packets = packetList.toArray(packets);

    return new Message(packets, roomid);
  }

  private void storePacket(Packet packet) {
    int roomid = packet.getRoomid();

    List<Packet> roomContainer = roomPackets.getOrDefault(roomid, null);
    if (roomContainer == null) {
      roomContainer = new ArrayList<>();
      roomPackets.put(roomid, roomContainer);
    }

    roomContainer.add(packet);
  }

  public Message getMessage() {
    return message;
  }
}
