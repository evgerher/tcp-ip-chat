package packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PacketFactory {
  private static volatile long packetId = 0;
  private PacketFactory() {}

  public static synchronized Packet[] generatePackets(byte[] bytes) {
    int from, end, step, part;
    int amount = bytes.length;
    int packetsAmount = amount % Packet.CONTENT_SIZE;
    boolean last;

    Packet packets[] = new Packet[packetsAmount];
    for (int i = 0; i < packetsAmount; i++) {
      step = (i + 1) * Packet.CONTENT_SIZE;
      from = i * Packet.CONTENT_SIZE;
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
      os.write(p.getBytes());
    }

    return os.toByteArray();
  }
}
