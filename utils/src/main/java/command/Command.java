package command;

public interface Command {
  String getKeyword();

  String executeCommand(String... parameters);

  String getShortDescription();
}
