package net.codekata.cloudkeytool;

import com.google.inject.AbstractModule;
import net.codekata.cloudkeytool.aws.AwsSecretsManagerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** DI */
public class CloudKeyToolModule extends AbstractModule {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(CloudKeyToolModule.class);

  // TODO: Contain command line options.
  private final String profile;

  public CloudKeyToolModule(String profile) {
    this.profile = profile;
  }

  @Override
  protected void configure() {
    install(new AwsSecretsManagerModule(profile));
  }
}
