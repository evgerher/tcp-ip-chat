package client;

import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.Message;

public class Client {
  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  private ClientSocket connection;

  public Client() {
    try {
      connection = new ClientSocket(this);
      connection.start();
    } catch (Exception e) {
      logger.error("oh no, I die {}", e.toString());  // TODO: remove this
    }
  }

  public void sendMessage(String content) {
    Message msg = new Message(new String(content).getBytes(), 1);

    try {
      connection.sendMessage(msg);
    } catch (Exception e) {
      logger.error(e.toString());  // TODO: remove this
    }
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
      if (s.equals(".stop"))
        stop = true;
      cl.sendMessage(s);
    }
  }
}
