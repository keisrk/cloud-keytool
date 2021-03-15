package net.codekata.cloudkeytool;

import static io.atlassian.fugue.Option.option;
import static net.codekata.cloudkeytool.Utils.unit;

import io.atlassian.fugue.Option;
import java.io.Console;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(App.class);

  /** Entry point. */
  public static void main(String[] args) {
    CommandSpec.create()
        .parse(args)
        .map(
            (CommandLine cmd) -> {
              Console console = System.console();
              Option<String> keystore = option(cmd.getOptionValue("keystore"));
              String storePasswd =
                  option(cmd.getOptionValue("storepass"))
                      .getOr(() -> new String(console.readPassword("Enter keystore password:")));
              Option<String> profile = option(cmd.getOptionValue("profile"));
              CloudKeyTool cKeyTool = new CloudKeyTool();
              cKeyTool.run(profile, keystore, option(storePasswd));
              return unit();
            });
  }
}
