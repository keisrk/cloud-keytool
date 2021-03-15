package net.codekata.cloudkeytool;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** For testing purpose only. */
public final class ResourceKeyStoreService implements KeyStoreService {
  private static final Logger logger =
      (Logger) LoggerFactory.getLogger(ResourceKeyStoreService.class);

  public final CompletableFuture<KeyStore> getKeyStore(String secretId, String password) {
    final InputStream is = getClass().getClassLoader().getResourceAsStream(secretId);
    return KeyStoreDeserializer.builder().input(is).password(password).build().deserialize();
  }
}
