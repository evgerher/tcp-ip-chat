package client.handlers;

import client.Client;
import client.ClientSocket;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.Message;
import packets.MessageAcceptor;

public class MessageProcessor implements MessageAcceptor {
  private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

  private final String COMMAND_SYMBOL = "/";
  private final Client client;
  private ClientSocket connection;
  private int roomid = 0;

  public MessageProcessor(Client client) {
    this.client = client;
    try {
      connection = new ClientSocket(this);
      connection.start();
    } catch (Exception e) {
      logger.error(e.toString());  // TODO: remove this
    }
  }

  public void process(String content) {
    if (content.startsWith(COMMAND_SYMBOL)) {
      parseCommand(content);
    } else {
      Message msg = new Message(new String(content).getBytes(), roomid, false);
      try {
        connection.sendMessage(msg);
      } catch (Exception e) {
        e.printStackTrace();
        logger.error(e.toString());  // TODO: remove this
      }
    }
  }

  private void parseCommand(String content) {
    if (content.startsWith("!stop")) {

    } else if (content.startsWith("/room")) { // Change message destination to specified room (int)
      int len = "/room".length(); // todo: make consts
      String number = content.substring(content.indexOf("/room") + len);
      number = number.trim();
      try {
        Integer id = Integer.parseInt(number);
        roomid = id;
        logger.info("/room {} command used", roomid);
      } catch (RuntimeException e) {
        logger.error("Room :: Could not parse roomid from [{}]", content);
      }
    } else if (content.startsWith("/register") || content.startsWith("/connect")) { // register on a new room on a server (int)
      try {
        sendCommand(content);
        logger.info("{} command used", content);
      } catch (RuntimeException e) {
        e.printStackTrace();
        logger.error("Register or Connect :: Could not parse roomid from [{}]", content);
      }
    }
  }

  private void sendCommand(String content) {
    byte[] bytes = content.getBytes();
    Message message = new Message(bytes, -1, true);
    try {
      connection.sendMessage(message);
    } catch (ExecutionException e) {
      e.printStackTrace(); // todo: logger here
    } catch (InterruptedException e) {
      e.printStackTrace(); // todo: logger here
    }
  }

  @Override
  public void acceptMessage(Message message) {
    client.acceptMessage(message); //todo: wtf
  }

  @Override
  public void setAnnotation(Object annotation) {
    // todo: me
  }

  @Override
  public Object getAnnotation() {
    return null;
  }
}
