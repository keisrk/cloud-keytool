package net.codekata.cloudkeytool;

import static io.atlassian.fugue.Suppliers.fromFunction;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.function.Function.identity;

import io.atlassian.fugue.Checked;
import io.atlassian.fugue.Unit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Plain old local JKS service. */
public class LocalKeyStoreService implements KeyStoreService {
  private static final Logger logger = LoggerFactory.getLogger(LocalKeyStoreService.class);

  public final CompletableFuture<KeyStore> getKeyStore(String secretId, String password) {
    return getSecretBinary(secretId)
        .thenCompose(
            is ->
                KeyStoreDeserializer.builder()
                    // TODO: Judge keystore type from file extension or file's naming convention.
                    .keyStoreType("JKS")
                    .input(is)
                    .password(password)
                    .build()
                    .deserialize());
  }

  /** TODO: Elaborate API from existing examples. */
  public final CompletableFuture<Unit> store(KeyStore ks, String filePath, String password) {
    final var path = Paths.get(filePath).toFile();
    final var output = Checked.<File, OutputStream, IOException>lift(FileOutputStream::new);
    return supplyAsync(fromFunction(output, path))
        .thenApply(result -> result.fold(Utils::throwRuntime, identity()))
        .thenCompose(
            os -> KeyStoreSerializer.builder().output(os).password(password).build().serialize());
  }

  private final CompletableFuture<InputStream> getSecretBinary(String secretId) {
    final var path = Paths.get(secretId).toFile();
    final var input = Checked.<File, InputStream, IOException>lift(FileInputStream::new);
    return supplyAsync(fromFunction(input, path))
        .thenApply(result -> result.fold(Utils::throwRuntime, identity()));
  }
}
