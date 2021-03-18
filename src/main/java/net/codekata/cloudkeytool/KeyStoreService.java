package net.codekata.cloudkeytool;

import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.util.concurrent.CompletableFuture;

public interface KeyStoreService {
  public CompletableFuture<KeyStore> getKeyStore(String secretId, PasswordProtection password);
}
