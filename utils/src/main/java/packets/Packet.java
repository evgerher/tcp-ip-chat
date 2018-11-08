package packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Packet {
  private static final Logger logger = LoggerFactory.getLogger(Packet.class);

  public final static int PACKET_SIZE = 256;
  public final static int MAX_CONTENT_SIZE = 256 - 4 - 4 - 4 - 4;
  public final int CONTENT_SIZE;

  private int id;
  private int part;
  private byte[] bytes;
  private boolean last;

  Packet(int id, int part, byte[] bytes, boolean last) {
    this.bytes = bytes;
    this.part = part;
    this.id = id;
    this.last = last;

    CONTENT_SIZE = PACKET_SIZE - bytes.length - 4 - 4 - 4 - 4;  // -bytes.length - sizeof(id + int(last) + part + packet_size)
  }

  public boolean isPart() {
    return part != -1;
  }

  public int getSize() {
    return bytes.length;
  }

  public long getId() {
    return id;
  }

  public long getPart() {
    return part;
  }

  public byte[] getContent() {
    return bytes;
  }

  public boolean isLast() {
    return last;
  }
  
  public byte[] toBytes() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    os.write(id);
    os.write(part);
    os.write(last ? 1: 0);
    os.write(CONTENT_SIZE);
    try {
      os.write(bytes);
    } catch (IOException e) {
      logger.error("PANIC");  // TODO: remove
    }
    return os.toByteArray();
  }

  public static Packet decode(byte[] bytes) {
    ByteBuffer bf = ByteBuffer.wrap(bytes);
    int id = bf.getInt(0);
    int part = bf.getInt(1);
    boolean last = bf.getInt(2) > 0;
    int size = bf.getInt(3);
    byte content[] = Arrays.copyOfRange(bytes, 16, size);

    return new Packet(id, part, content, last);
  }
}
