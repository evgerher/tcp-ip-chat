package packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {
  private static final Logger logger = LoggerFactory.getLogger(Message.class);

  private Packet[] packets;
  private byte[] content;

  public Message(byte bytes[]) {
    packets = PacketFactory.generatePackets(bytes);
  }

  public Message(Packet[] packets) {
    this.packets = packets;
  }

  public Packet[] getPackets() {
    return packets;
  }

  public byte[] getContent() {  // TODO: REMOVE EXCEPTION
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      for (Packet p : packets) {
        os.write(p.getContent());
      }
    } catch (Exception e) {
      logger.error("Panic");
    }

    return os.toByteArray();
  }

  @Override
  public String toString() {
    return new String(getContent());
  }
}
