package net.codekata.cloudkeytool;

import static io.atlassian.fugue.Checked.now;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.function.Function.identity;
import static net.codekata.cloudkeytool.Utils.unit;

import io.atlassian.fugue.Try;
import io.atlassian.fugue.Unit;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Serializer for keystore */
@Builder
final class KeyStoreSerializer {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(KeyStoreSerializer.class);
  private final KeyStore keyStore;
  private final OutputStream output;
  private final PasswordProtection password;

  /** Dumps keystore to the given byte stream. */
  private final Unit deserializeOrThrow() throws IOException, GeneralSecurityException {
    keyStore.store(output, password.getPassword());
    logger.info("Stored {}.", keyStore.getType());
    return unit();
  }

  /** Synchronously dumps keystore. */
  public final Try<Unit> serializeSync() {
    return now(this::deserializeOrThrow);
  }

  /** Dumps keystore. */
  public final CompletableFuture<Unit> serialize() {
    return supplyAsync(this::serializeSync)
        .thenApplyAsync(result -> result.fold(Utils::throwRuntime, identity()));
  }
}
