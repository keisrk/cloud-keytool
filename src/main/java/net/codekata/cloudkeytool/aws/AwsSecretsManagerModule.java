package net.codekata.cloudkeytool.aws;

import static io.atlassian.fugue.Option.fromOptional;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import net.codekata.cloudkeytool.KeyStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.profiles.ProfileProperty;
import software.amazon.awssdk.regions.Region;

/** DI for AWS Secrets Manager backend. */
public final class AwsSecretsManagerModule extends AbstractModule {
  private static final Logger logger =
      (Logger) LoggerFactory.getLogger(AwsSecretsManagerModule.class);
  private final String profile;

  public AwsSecretsManagerModule(String profile) {
    // TODO: What if profile is None?
    this.profile = profile;
  }

  @Override
  protected final void configure() {
    bind(KeyStoreService.class).to(AwsKeyStoreService.class);
  }

  @Provides
  public final AwsCredentialsProvider getCredentials() {
    return ProfileCredentialsProvider.create(profile);
  }

  @Provides
  public final Region getRegion() {
    return fromOptional(ProfileFile.defaultProfileFile().profile(profile))
        .map(p -> p.properties().get(ProfileProperty.REGION))
        .map(Region::of)
        .get();
  }
}
