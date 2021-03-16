package net.codekata.cloudkeytool;

import static io.atlassian.fugue.Option.fromOptional;

import java.util.Optional;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "cloud-keytool",
    version = "0.0.1-SNAPSHOT",
    mixinStandardHelpOptions = true,
    description =
        "\nExtend keytool to store, manage and retrieve secrets from a cloud secret manager.\n",
    footer = "\nPlease report issues at https://github.com/keisrk/cloud-keytool/issues")
final class CloudKeyToolCommand implements Callable<Integer> {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(CloudKeyToolCommand.class);

  @Option(names = "--list", description = "Lists entries in a keystore")
  private boolean list;

  @Option(
      names = "--importkeystore",
      description = "Imports one or all entries from another keystore")
  private boolean importKeyStore;

  @Option(names = "--keystore", paramLabel = "KEYSTORE", description = "The keystore location")
  private Optional<String> keystore;

  @Option(
      names = "--storepass",
      paramLabel = "STORE_PASSWD",
      description = "password protecting the keystore")
  private Optional<String> storePass;

  // TODO: Move it to subcommand.
  @Option(names = "--profile", paramLabel = "PROFILE", description = "profile used to access AWS")
  private Optional<String> profile;

  public Integer call() throws Exception {
    final CloudKeyTool cKeyTool = new CloudKeyTool();
    if (list) {
      cKeyTool.run(fromOptional(profile), fromOptional(keystore), fromOptional(storePass));
    } else {
      logger.info("No task to do.");
    }

    return 0;
  }
}
