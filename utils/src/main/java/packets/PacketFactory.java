package packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PacketFactory {
  private static volatile int packetId = 0;
  private PacketFactory() {}

  public static synchronized Packet[] generatePackets(byte[] bytes) {
    int from, end, step, part;
    int amount = bytes.length;
    int packetsAmount = amount / Packet.MAX_CONTENT_SIZE + amount % Packet.MAX_CONTENT_SIZE > 0 ? 1: 0;
    boolean last;

    Packet packets[] = new Packet[packetsAmount];
    for (int i = 0; i < packetsAmount; i++) {
      step = (i + 1) * Packet.MAX_CONTENT_SIZE;
      from = i * Packet.MAX_CONTENT_SIZE;
      end = (step) <= amount? step: amount;
      part = (packetsAmount != 1) ? i: -1;
      last = (i == (packetsAmount - 1));

      byte b[] = Arrays.copyOfRange(bytes, from, end);
      Packet p = new Packet(packetId, part, b, last);
      packets[i] = p;
    }

    packetId++;
    return packets;
  }

  public static byte[] toBytes(Packet[] packets) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    for (Packet p: packets) {
      os.write(p.getContent());
    }

    return os.toByteArray();
  }
}
