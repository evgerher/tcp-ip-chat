package client.handlers;

import client.Client;
import client.ClientSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.Message;
import packets.MessageAcceptor;

public class MessageProcessor implements MessageAcceptor {
  private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

  private final String COMMAND_SYMBOL = "!";
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
      Message msg = new Message(new String(content).getBytes(), roomid);

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

    } else if (content.startsWith("!room")) {
      int len = "!room".length();
      String number = content.substring(len);
      try {
        Integer id = Integer.parseInt(number);
        roomid = id;
      } catch (RuntimeException e) {
        logger.error("Could not parse roomid from [{}]", content);
      }
    }
  }

  @Override
  public void acceptMessage(Message message) {
    client.acceptMessage(message); //todo: wtf
  }
}
