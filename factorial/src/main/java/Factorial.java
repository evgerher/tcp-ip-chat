import command.Command;

public class Factorial implements Command {

  @Override
  public String getKeyword() {
    return "factorial";
  }

  @Override
  public String executeCommand(String... parameters) {
    Integer value = Integer.parseInt(parameters[0]);
    Integer fact = factorial(value);
    return fact.toString();
  }

  private Integer factorial(Integer value) {
    if (value == 0)
      return 1;
    return value * factorial(value - 1);
  }

  @Override
  public String getShortDescription() {
    return "Returns factorial of a specified number";
  }
}
