import command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Factorial implements Command {
  private static final Logger logger = LoggerFactory.getLogger(Factorial.class);

  @Override
  public String getKeyword() {
    return "factorial";
  }

  @Override
  public String executeCommand(String... parameters) {
    try {
      logger.info("Executing command with value {}", parameters[0]);
      Integer value = Integer.parseInt(parameters[0]);
      Integer fact = factorial(value);
      return fact.toString();
    } catch (RuntimeException e) {
      logger.error("Unable to read integer from first parameter {}", parameters[0]);
      return null;
    }
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
