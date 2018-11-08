package packets;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {
  private static final Logger logger = LoggerFactory.getLogger(Message.class);

  private Packet[] packets;

  public Message(byte bytes[]) {
    packets = PacketFactory.generatePackets(bytes);
  }

  public Message(Packet[] packets) {
    this.packets = packets;
  }

  public Packet[] getPackets() {
    return packets;
  }

  @Override
  public String toString() {
    try {
      return new String(PacketFactory.toBytes(packets));
    } catch (IOException e) {
      logger.error("PANIC");
    }
    
    return null;
  }
}
