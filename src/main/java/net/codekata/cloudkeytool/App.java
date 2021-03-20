package net.codekata.cloudkeytool;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.util.Modules.combine;
import static io.atlassian.fugue.Option.option;
import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.AbstractModule;
import io.atlassian.fugue.Unit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.codekata.cloudkeytool.aws.AwsSecretsManagerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.RunAll;

public final class App {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(App.class);

  private static final CompletableFuture<Unit> run(CommandLine cmd) throws Exception {
    final var sub = cmd.getSubcommands().values().stream().findFirst();
    if (cmd.isUsageHelpRequested() || sub.map(c -> c.isUsageHelpRequested()).orElse(false)) {
      return completedFuture(Utils.unit());
    }

    final var keytool =
        option(cmd.<CloudKeyToolModule>getExecutionResult())
            .getOrThrow(() -> new Exception("No keytool"));
    final var provider =
        sub.map(CommandLine::<AbstractModule>getExecutionResult)
            .flatMap(Optional::ofNullable)
            .orElseThrow(() -> new Exception("No provider"));

    return createInjector(combine(keytool, provider)).getInstance(CloudKeyTool.class).call();
  }

  /** Entry point. */
  public static final void main(String[] args) {
    final var cmd =
        new CommandLine(new CloudKeyToolCommand())
            .addSubcommand(new AwsSecretsManagerCommand())
            .setExecutionStrategy(new RunAll());

    try {
      final var exitCode = cmd.execute(args);
      if (exitCode != 0) {
        System.exit(exitCode);
      }
      run(cmd).join();
      logger.info("Command completed.");
      System.exit(0);
    } catch (Exception e) {
      logger.error(e.getMessage());
      System.exit(1);
    }
  }
}
