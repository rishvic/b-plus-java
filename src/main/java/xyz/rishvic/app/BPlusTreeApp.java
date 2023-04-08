package xyz.rishvic.app;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.rishvic.util.BPlusTree;

@Command(
    name = "b+demo",
    mixinStandardHelpOptions = true,
    version = "B+ Tree demo 1.0-SNAPSHOT",
    description = "A CLI application demonstrating B+ Tree operations.")
public class BPlusTreeApp implements Callable<Integer> {

  final Logger logger = LoggerFactory.getLogger(BPlusTreeApp.class);

  @Option(
      names = {"-B", "--branching-factor"},
      description = "Branching factor of B+ Tree",
      defaultValue = "3")
  int bf;

  @Override
  public Integer call() throws Exception {
    BPlusTree<Integer> tree;
    try {
      tree = new BPlusTree<>(bf);
    } catch (IllegalArgumentException err) {
      logger.error("Illegal argument passed", err);
      return 1;
    }

    LineReader reader = LineReaderBuilder.builder().build();
    final String prompt = ">>> ";
    int interruptCount = 0;
    boolean verbose = false;

    List<String> mutableCommands = Arrays.asList("CLEAR", "ADD");

    loop:
    while (true) {
      String line = null;
      try {
        line = reader.readLine(prompt);
        interruptCount = 0;
        final StringTokenizer tokenizer = new StringTokenizer(line);
        if (!tokenizer.hasMoreTokens()) continue;

        String command = tokenizer.nextToken();
        switch (command.toUpperCase()) {
          case "EXIT":
          case "QUIT":
            break loop;

          case "PRINT":
            System.out.printf("%s", tree.toPrettyString());
            break;

          case "CLEAR":
            tree.clear();
            break;

          case "ADD":
            while (tokenizer.hasMoreTokens()) {
              String token = tokenizer.nextToken();
              int val;
              try {
                val = Integer.parseInt(token, 10);
              } catch (NumberFormatException e) {
                logger.error("Invalid integer: {}", token);
                continue;
              }
              tree.add(val);
              logger.debug("Added {}", val);
            }
            break;

          case "SET":
            if (!tokenizer.hasMoreTokens()) break;

            String param = tokenizer.nextToken();
            switch (param.toUpperCase()) {
              case "VERBOSE":
                verbose = true;
                break;

              case "NOVERBOSE":
                verbose = false;
                break;

              default:
                logger.warn("No such parameter: {}", param);
                break;
            }
            break;

          default:
            logger.warn("Unknown command: {}", command);
            break;
        }

        if (verbose && mutableCommands.contains(command.toUpperCase())) {
          System.out.printf("%s", tree.toPrettyString());
        }
      } catch (UserInterruptException ignored) {
        interruptCount++;
        if (interruptCount == 2) {
          logger.info("Interrupt received again, exiting...");
          break;
        }
        logger.info("Press Ctrl+C again to exit");
      } catch (EndOfFileException e) {
        break;
      }
    }
    return 0;
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new BPlusTreeApp()).execute(args);
    System.exit(exitCode);
  }
}
