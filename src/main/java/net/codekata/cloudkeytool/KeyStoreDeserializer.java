package net.codekata.cloudkeytool;

import static io.atlassian.fugue.Suppliers.fromFunction;
import static io.atlassian.fugue.Suppliers.ofInstance;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.function.Function.identity;

import io.atlassian.fugue.Checked;
import io.atlassian.fugue.Suppliers;
import io.atlassian.fugue.Try;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Deserializer for PKCS12 keystore */
@Builder
public class KeyStoreDeserializer {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(KeyStoreDeserializer.class);
  @Builder.Default private final String keyStoreType = "PKCS12";
  private final InputStream input;
  private final String password;

  /** Loads PKCS12 keystore from the given byte stream. */
  private final KeyStore deserializeOrThrow(KeyStore ks)
      throws IOException, GeneralSecurityException {
    ks.load(input, password.toCharArray());
    return ks;
  }

  /** Synchronously loads PKCS12 keystore. */
  public final Try<KeyStore> deserializeSync() {
    final var init = Checked.<String, KeyStore, KeyStoreException>lift(KeyStore::getInstance);
    final var deserialize = Checked.<KeyStore, KeyStore, Exception>lift(this::deserializeOrThrow);
    final var supInit = fromFunction(init, keyStoreType);
    final Supplier<Function<Try<KeyStore>, Try<KeyStore>>> supDeserialize =
        ofInstance(ks -> ks.flatMap(deserialize));
    return Suppliers.ap(supInit, supDeserialize).get();
  }

  /** Loads PKCS12 keystore. */
  public final CompletableFuture<KeyStore> deserialize() {
    return supplyAsync(this::deserializeSync)
        .thenApply(result -> result.fold(Utils::throwRuntime, identity()));
  }
}
