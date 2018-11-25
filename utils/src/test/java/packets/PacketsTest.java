package packets;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class PacketsTest {
  @Test
  public void complexSeveralPackets() throws IOException {
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

      Packet[] packets = PacketFactory.generatePackets(msg.getBytes(), 0, false);
      final int expectedAmount = 3;
      Assert.assertEquals(expectedAmount, packets.length);

      final int expectedLastLength = length - 2 * Packet.MAX_CONTENT_SIZE;
      Assert.assertEquals(expectedLastLength, packets[2].getSize());

      Assert.assertFalse(packets[0].isLast());

      byte bytes[] = PacketFactory.toBytes(packets);
      Assert.assertEquals(msg, new String(bytes));
  }

  @Test
  public void checkPacketSeparation() throws Exception {
    final String msg240 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
        + "Maecenas vel urna pellentesque, eleifend lacus eget, accumsan ante. Mauris "
        + "ultrices, nulla vitae facilisis volutpat, tellus orci porta urna, a interdum "
        + "arcu ipsum at arcu. Vivamus id.";
    final String msg226 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
        + "Maecenas vel urna pellentesque, eleifend lacus eget, accumsan ante. Mauris "
        + "ultrices, nulla vitae facilisis volutpat, tellus orci porta urna, a interdum "
        + "arcu ipsum at arc";

    Packet[] packets1 = PacketFactory.generatePackets(msg240.getBytes(), 0, false);
    Assert.assertEquals(2, packets1.length);

    Packet[] packets2 = PacketFactory.generatePackets(msg226.getBytes(), 0, false);
    Assert.assertEquals(1, packets2.length);
  }

  @Test
  public void checkSingle() throws Exception {
    final String msg = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
        + "Mauris diam dui, pulvinar nec mi at, gravida commodo turpis. Nam ac sapien metus.";
    final int size = 138;
    final int expectedLength = 1;

    Packet[] packets = PacketFactory.generatePackets(msg.getBytes(), 0, false);
    Assert.assertEquals(expectedLength, packets.length);

    Packet packet = packets[0];
    Assert.assertEquals(msg, new String(packet.getContent()));
    Assert.assertEquals(size, packet.getSize());
  }
}
