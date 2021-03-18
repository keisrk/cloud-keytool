package net.codekata.cloudkeytool;

import io.atlassian.fugue.Option;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Controller for KeyStore entity */
public final class CloudKeyTool implements Runnable {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(CloudKeyTool.class);
  private final KeyStoreService service;
  private final Option<ListEntries> listEntries;
  private final Option<ImportKeyStore> importKeyStore;

  @Inject
  public CloudKeyTool(
      KeyStoreService service,
      Option<ListEntries> listEntries,
      Option<ImportKeyStore> importKeyStore) {
    this.service = service;
    this.listEntries = listEntries;
    this.importKeyStore = importKeyStore;
  }

  @Override
  public final void run() {
    logger.info("Calling on {}", service.getClass().getSimpleName());

    if (listEntries.isDefined()) {
      listEntries();
    } else if (importKeyStore.isDefined()) {
      importKeyStore();
    } else {
      logger.error("Nothing to do.");
    }
  }

  // Model it as a stream of entries?
  final void listEntries() {
    /* a.k.a. doPrintEntries */
    final var ls = listEntries.get();
    service
        .getKeyStore(ls.keystore, ls.storePass)
        .thenApplyAsync(
            ks -> {
              logger.info("Successfully loaded {}", ls.keystore);
              final var repo = new KeyRepository(ks);
              ls.dump(repo);
              return 0;
            })
        .join();
  }

  final void importKeyStore() {
    /* a.k.a. doimportKeyStore */
    final var is = importKeyStore.get();
    service
        .getKeyStore(is.srcKeyStore, is.srcStorePass)
        .thenApply(
            ks -> {
              logger.info("Successfully loaded {}", is.srcKeyStore);
              return new KeyRepository(ks);
            })
        .thenCombineAsync(
            is.getDest(),
            (src, dst) -> {
              logger.info("{}, {}", src, dst);
              return 0;
            });
  }

  @Builder
  static final class ListEntries {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(ListEntries.class);
    private final String keystore;
    private final PasswordProtection storePass;

    ListEntries(String keystore, PasswordProtection storePass) {
      this.keystore = keystore;
      this.storePass = storePass;
    }

    final void dump(KeyRepository repo) {
      repo.aliases().forEachRemaining(alias -> dump(repo, alias));
    }

    final void dump(KeyRepository repo, String alias) {
      try {
        final var cert = repo.keyStore().getCertificate(alias);
        final var priv =
            ((KeyStore.PrivateKeyEntry) repo.keyStore().getEntry(alias, storePass)).getPrivateKey();
        logger.info("Entry aliased {}", alias);
        logger.info("Certificate of {}, {}", cert.getPublicKey().getAlgorithm(), cert.getType());
        logger.info("Private key of {}, {}", priv.getAlgorithm(), priv.getFormat());
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }
  }

  @Builder
  static final class ImportKeyStore {
    private final String srcKeyStore;
    private final PasswordProtection srcStorePass;
    private final String destKeyStore;
    private final PasswordProtection destStorePass;

    final void foo(KeyRepository src, KeyRepository dst) {
      src.aliases()
          .forEachRemaining(
              alias -> {
                try {
                  // FIXME: It should be setKeyEntry. Read the source of keytool.
                  dst.keyStore().deleteEntry(alias);
                } catch (Exception e) {
                  logger.error("Oh no");
                }
              });
    }

    final CompletableFuture<KeyRepository> getDest() {
      return new LocalKeyStoreService()
          .getKeyStore(destKeyStore, destStorePass)
          .thenApplyAsync(KeyRepository::new);
    }
  }
}
