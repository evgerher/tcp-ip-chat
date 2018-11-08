package packets;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class PacketsTest {
  @Test
  public void createSeveralPackets() throws IOException {
      final String msg = "Lorem ipsum dolor sit amet, "
          + "consectetur adipiscing elit. Donec ultrices quam "
          + "non lacus bibendum imperdiet. Aliquam in quam eleifend, "
          + "varius metus vel, egestas mi. Mauris sollicitudin vestibulum "
          + "vehicula. Donec in eros mattis mauris congue vulputate sit amet "
          + "in odio. Praesent venenatis dapibus neque nec pellentesque. "
          + "Nunc felis lacus, luctus eu erat vitae, eleifend cursus sapien. "
          + "Proin sodales velit vitae urna posuere, eget lobortis justo tristique. "
          + "Duis nec leo ex. Mauris convallis viverra amet.";
      final int length = 500;

      Packet[] packets = PacketFactory.generatePackets(msg.getBytes());
      final int expectedAmount = length / Packet.MAX_CONTENT_SIZE + 1;
      Assert.assertEquals(expectedAmount, packets.length);

      final int expectedLastLength = length - 2 * Packet.MAX_CONTENT_SIZE;
      Assert.assertEquals(expectedLastLength, packets[2].getSize());

      Assert.assertFalse(packets[0].isLast());

      byte bytes[] = PacketFactory.toBytes(packets);
      Assert.assertEquals(msg, new String(bytes));
  }
}
