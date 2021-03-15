package net.codekata.cloudkeytool;

import static io.atlassian.fugue.Checked.now;
import static io.atlassian.fugue.Try.failure;
import static org.apache.commons.cli.Option.builder;

import io.atlassian.fugue.Try;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// list and import are mutually exclusive. Use OptionGroup.
class CommandSpec {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(CommandSpec.class);
  private static final String APP_NAME = "cloud-keytool";
  private static final String HEADER =
      "Extend keytool to store, manage and retrieve secrets from a cloud secret manager.\n\n";
  private static final String FOOTER =
      "\nPlease report issues at https://github.com/keisrk/cloud-keytool/issues";

  private final Options options;

  CommandSpec() {
    this.options = new Options();
  }

  static final CommandSpec create() {
    final CommandSpec cs = new CommandSpec();
    cs.init();
    return cs;
  }

  final Try<CommandLine> parse(String[] args) {
    CommandLineParser parser = new DefaultParser();
    return now(() -> parser.parse(options, args))
        .recoverWith(
            ParseException.class,
            e -> {
              logger.error(e.getMessage());
              help();
              return failure(e);
            });
  }

  /** Prints help message. */
  final void help() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(APP_NAME, HEADER, options, FOOTER);
  }

  /** Populates options. */
  final void init() {
    // Subcommands to determine the action.
    // TODO: Elaborate descriptions.
    // TODO: Service Provider Group.
    final var group = new OptionGroup();
    group.addOption(builder().longOpt("list").desc("Lists entries in a keystore").build());
    group.addOption(
        builder()
            .longOpt("importkeystore")
            .desc("Imports one or all entries from another keystore")
            .build());
    options.addOptionGroup(group);

    // -list Modeled after original keytool command
    options.addOption(
        builder()
            .longOpt("keystore")
            .argName("KEYSTORE")
            .hasArg()
            .desc("The keystore location")
            .build());
    options.addOption(
        builder()
            .longOpt("storepass")
            .argName("KEYSTORE_PASSWD")
            .hasArg()
            .desc("password protecting the keystore")
            .build());

    // -importkeystore between local file system and cloud services.
    options.addOption(
        builder()
            .longOpt("srckeystore")
            .argName("SRC_KEYSTORE")
            .hasArg()
            .desc("The src keystore")
            .build());
    options.addOption(
        builder()
            .longOpt("srcalias")
            .argName("SRC_KEYSTORE_ALIAS")
            .hasArg()
            .desc("The alias of an entry in the keystore")
            .build());
    options.addOption(
        builder()
            .longOpt("dstkeystore")
            .argName("DST_KEYSTORE")
            .hasArg()
            .desc("The destination keystore")
            .build());
    options.addOption(
        builder()
            .longOpt("dstalias")
            .argName("DST_KEYSTORE_ALIAS")
            .hasArg()
            .desc("The alias of an entry in the keystore")
            .build());

    // AWS specific setting
    options.addOption(
        builder("p")
            .longOpt("profile")
            .argName("PROFILE")
            .hasArg()
            .desc("profile used to access AWS")
            .build());
  }
}
