package xyz.rishvic.app;

import java.util.concurrent.Callable;
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
    for (int elem :
        new int[] {
          36, 92, 93, 63, 69, 38, 44, 60, 23, 45, 36, 25, 31, 94, 29, 91, 9, 9, 80, 65, 23
        }) tree.add(elem);
    System.out.println(tree.toPrettyString());
    return 0;
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new BPlusTreeApp()).execute(args);
    System.exit(exitCode);
  }
}
