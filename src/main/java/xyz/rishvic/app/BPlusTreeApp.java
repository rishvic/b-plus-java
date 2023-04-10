/*
 * Copyright 2023 Rishvic Pushpakaran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    version = "B+ Tree demo 0.1.0",
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

    List<String> mutableCommands = Arrays.asList("CLEAR", "ADD", "REMOVE");

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

          case "CONTAINS":
            while (tokenizer.hasMoreTokens()) {
              String token = tokenizer.nextToken();
              int val;
              try {
                val = Integer.parseInt(token, 10);
              } catch (NumberFormatException e) {
                logger.error("Invalid integer: {}", token);
                continue;
              }
              System.out.printf("%d: %b\n", val, tree.contains(val));
              logger.debug("Added {}", val);
            }
            break;

          case "FIRST":
            System.out.printf("FIRST: %d\n", tree.first());
            break;

          case "LAST":
            System.out.printf("LAST: %d\n", tree.last());
            break;

          case "IS":
            while (tokenizer.hasMoreTokens()) {
              String condition = tokenizer.nextToken();
              if (condition.equalsIgnoreCase("EMPTY")) {
                System.out.printf("EMPTY: %b\n", tree.isEmpty());
              } else {
                logger.warn("Unknown condition: {}", condition);
              }
            }
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

          case "REMOVE":
            while (tokenizer.hasMoreTokens()) {
              String token = tokenizer.nextToken();
              int val;
              try {
                val = Integer.parseInt(token, 10);
              } catch (NumberFormatException e) {
                logger.error("Invalid integer: {}", token);
                continue;
              }
              boolean removed = tree.remove(val);
              logger.debug("Removed {}? {}", val, removed);
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
      } catch (UserInterruptException e) {
        if (!e.getPartialLine().equals("")) {
          interruptCount = 0;
          continue;
        }

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
