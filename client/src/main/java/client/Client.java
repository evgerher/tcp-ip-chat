package client;

import client.handlers.MessageProcessor;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.Message;

public class Client {
  private static final Logger logger = LoggerFactory.getLogger(Client.class);
  private final MessageProcessor messageProcessor;

  public Client() {
    messageProcessor = new MessageProcessor(this);
  }

  public void sendMessage(String content) {
    messageProcessor.process(content);
  }

  public void acceptMessage(Message msg) {
    logger.info("USER RECEIVED MESSAGE [{}]", msg.toString());
  }

  public static void main(String[] args) {
    Client cl = new Client();
    boolean stop = false;

    Scanner sc = new Scanner(System.in);
    while (!stop) {
      String s = sc.nextLine();
      if (s.startsWith("/stop")) {
        stop = true;
      }
      cl.sendMessage(s);
    }
  }
}
