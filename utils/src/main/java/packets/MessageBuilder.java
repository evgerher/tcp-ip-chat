package packets;

import java.util.LinkedList;
import java.util.List;

public class MessageBuilder {
  private Message message;
  List<Packet> packetList; // Reimplement because messages may come from different chats
  private boolean finished;

  public MessageBuilder() {
    finished = false;
    packetList = new LinkedList<>();
  }

  public void acceptBytes(byte bytes[]) {  // Reimplement in order to work with different chats
    Packet packet = Packet.decode(bytes);
    packetList.add(packet);
    if (packet.isLast()) {
      finished = true;
      Packet[] packets = new Packet[packetList.size()];
      packets = packetList.toArray(packets);

      message = new Message(packets);
    }
  }

  public boolean isConstructed() {
    return finished;
  }

  public Message getMessage() {
    return message;
  }
}
