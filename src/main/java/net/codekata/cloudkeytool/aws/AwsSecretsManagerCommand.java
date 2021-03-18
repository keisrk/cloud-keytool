package net.codekata.cloudkeytool.aws;

import com.google.inject.AbstractModule;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "aws", description = "\nAWS Secrets Manager.\n")
public final class AwsSecretsManagerCommand implements Callable<AbstractModule> {
  private static final Logger logger =
      (Logger) LoggerFactory.getLogger(AwsSecretsManagerCommand.class);

  @Option(names = "--profile", paramLabel = "PROFILE", description = "profile used to access AWS")
  private Optional<String> profile;

  public final AbstractModule call() throws Exception {
    return new AwsSecretsManagerModule(profile.get());
  }
}
