package packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {
  private static final Logger logger = LoggerFactory.getLogger(Message.class);

  private final Packet[] packets;
  private final int room;
  private final boolean isCommand;

  public Message(byte bytes[], int room, boolean isCommand) {
    this.room = room;
    this.isCommand = isCommand;
    packets = PacketFactory.generatePackets(bytes, room, isCommand);
  }

  public Message(Packet[] packets) {
    this.isCommand = packets[0].isCommand();
    this.room = packets[0].getRoomid();
    this.packets = packets;
  }

  public Packet[] getPackets() {
    return packets;
  }

  public int getRoom() {
    return room;
  }

  public byte[] getContent() {  // TODO: REMOVE EXCEPTION
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      for (Packet p : packets) {
        os.write(p.getContent());
      }
    } catch (Exception e) {
      logger.error(e.toString());
    }

    return os.toByteArray();
  }

  @Override
  public String toString() {
    String s = String.format("%s :: room %d", new String(getContent()), room);
    return s;
  }

  public boolean isCommand() {
    return isCommand;
  }
}
