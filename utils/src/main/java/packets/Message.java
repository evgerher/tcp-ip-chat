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

  public Message(byte bytes[], int room) {
    this.room = room;
    packets = PacketFactory.generatePackets(bytes, room);
  }

  public Message(Packet[] packets, int room) {
    this.packets = packets;
    this.room = room;
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
    return new String(getContent());
  }
}
