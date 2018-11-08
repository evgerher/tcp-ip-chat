package packets;

public class Packet {
  public final static int PACKET_SIZE = 256;
//  public final static int CONTENT_SIZE = PACKET_SIZE - 8 - 8 - 4;  // Remove first 3 fields
  public final static int CONTENT_SIZE = 236;  // Remove first 3 fields

  private long id;
  private long part;
  private byte[] bytes;
  private boolean last;

  Packet(long id, long part, byte[] bytes, boolean last) {
    this.bytes = bytes;
    this.part = part;
    this.id = id;
    this.last = last;
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

  public byte[] getBytes() {
    return bytes;
  }

  public boolean isLast() {
    return last;
  }
}
