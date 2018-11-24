package packets;

import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Packet {
  private static final Logger logger = LoggerFactory.getLogger(Packet.class);

  public final static int PACKET_SIZE = 256;
  public final static int FIELDS_SIZE = 4 * 5;
  public final static int MAX_CONTENT_SIZE = 256 - FIELDS_SIZE;
  public final int CONTENT_SIZE;

  private final int id;
  private final int part;
  private final byte[] bytes;
  private final boolean last;
  private final int roomid;

  protected Packet(int id, int part, byte[] bytes, boolean last, int roomid) {
    this.bytes = bytes;
    this.part = part;
    this.id = id;
    this.last = last;
    this.roomid = roomid;

    CONTENT_SIZE = bytes.length + 4 + 4 + 4 + 4;  // bytes.length + sizeof(id + int(last) + part + packet_size + roomid)
    logger.debug("Built packet id={} part={}, last={}, roomid={}", id, part, last, roomid);
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

  public int getRoomid() {
    return roomid;
  }

  public ByteBuffer byteBuffer() {
    ByteBuffer bf = ByteBuffer.allocate(CONTENT_SIZE);
    bf.putInt(id);
    bf.putInt(part);
    bf.putInt(last ? 1: 0);
    bf.putInt(CONTENT_SIZE);
    bf.putInt(roomid);
    bf.put(bytes);
    bf.flip();

    return bf;
  }

  public static Packet decode(ByteBuffer bf) {
    int id = bf.getInt();
    int part = bf.getInt();
    boolean last = bf.getInt() > 0;
    int size = bf.getInt();
    int roomid = bf.getInt();

    byte content[] = new byte[size];
    bf.get(content, FIELDS_SIZE, size - FIELDS_SIZE);

    logger.debug("Decoded packet id={} part={}, last={}, roomid={}", id, part, last, roomid);
    return new Packet(id, part, content, last, roomid);
  }
}
