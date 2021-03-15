package net.codekata.cloudkeytool.aws;

import static io.atlassian.fugue.Option.option;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import net.codekata.cloudkeytool.KeyStoreDeserializer;
import net.codekata.cloudkeytool.KeyStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.BytesWrapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

/** AWS Secrets Manager backend. */
public final class AwsKeyStoreService implements KeyStoreService {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(AwsKeyStoreService.class);
  private final Region region;
  private final AwsCredentialsProvider credentialsProvider;

  @Inject
  public AwsKeyStoreService(AwsCredentialsProvider credentialsProvider, Region region) {
    this.credentialsProvider = credentialsProvider;
    this.region = region;
  }

  public final CompletableFuture<KeyStore> getKeyStore(String secretId, String password) {
    return getSecretBinary(secretId)
        .thenCompose(
            is ->
                KeyStoreDeserializer.builder().input(is).password(password).build().deserialize());
  }

  private final CompletableFuture<InputStream> getSecretBinary(String secretId) {
    final var client =
        SecretsManagerAsyncClient.builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .build();

    final var request = GetSecretValueRequest.builder().secretId(secretId).build();

    return client
        .getSecretValue(request)
        .thenApply(v -> option(v.secretBinary()).map(BytesWrapper::asInputStream))
        .thenApply(opt -> opt.getOrThrow(() -> new RuntimeException("Not present.")));
  }
}
