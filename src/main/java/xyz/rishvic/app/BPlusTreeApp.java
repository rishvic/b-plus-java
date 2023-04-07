package xyz.rishvic.app;

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "b+demo",
    mixinStandardHelpOptions = true,
    version = "B+ Tree demo 1.0-SNAPSHOT",
    description = "A CLI application demonstrating B+ Tree operations.")
public class BPlusTreeApp implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    System.out.println("Hello World!");
    return 0;
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new BPlusTreeApp()).execute(args);
    System.exit(exitCode);
  }
}
