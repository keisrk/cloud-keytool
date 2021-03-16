package net.codekata.cloudkeytool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class App {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(App.class);

  /** Entry point. */
  public static void main(String[] args) {
    final int exitCode = new CommandLine(new CloudKeyToolCommand()).execute(args);
    System.exit(exitCode);
  }
}
