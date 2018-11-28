package server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.Message;
import packets.MessageAcceptor;
import packets.MessageBuilder;

public class CommandProcessor implements MessageAcceptor<AsynchronousSocketChannel> {
  private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

  private final Server server;
  private final MessageBuilder messageBuilder;

  private AsynchronousSocketChannel annotation;

  public CommandProcessor(Server server) {
    this.server = server;
    messageBuilder = new MessageBuilder(this);
  }

  @Override
  public void acceptMessage(Message message) {
    //todo: me
    if (message.isCommand())
      processCommand(message);
  }

  @Override
  public synchronized void setAnnotation(AsynchronousSocketChannel annotation) {
    this.annotation = annotation;
  }

  @Override
  public AsynchronousSocketChannel getAnnotation() {
    return annotation;
  }

  private void processCommand(Message message) {
    String cmd = new String(message.getContent());
    if (cmd.contains("/register")) { // todo: wtf, startsWith(..) does not work properly, equals(..) also, probably because of bytes internally
      int len = "/register".length();
      String substring = cmd.substring(cmd.indexOf("/register") + len, cmd.length());
//      String substring = cmd.substring(len);
      try {
        Integer id = Integer.parseInt(substring.trim());
        server.registerRoom(id);
        server.connectClientToRoom(getAnnotation(), id);
        logger.debug("Client {} registered & connected to room {}", getAnnotation(), id);
      } catch (RuntimeException e) {
        e.printStackTrace();
        logger.error("Register :: Could not parse integer for string [{}]", cmd); // todo: better message for logger
      }
    } else if (cmd.contains("/connect")) {
      int len = "/connect".length();
      String substring = cmd.substring(cmd.indexOf("/connect") + len, cmd.length());
      try {
        Integer id = Integer.parseInt(substring.trim());
        server.connectClientToRoom(getAnnotation(), id);
      } catch (RuntimeException e) {
        e.printStackTrace();
        logger.error("Connect :: Could not parse integer for string [{}]", cmd); // todo: better message for logger
      }
    }
  }

  public void process(ByteBuffer bf, AsynchronousSocketChannel source) {
    setAnnotation(source);
    messageBuilder.acceptPacket(bf);
  }
}
