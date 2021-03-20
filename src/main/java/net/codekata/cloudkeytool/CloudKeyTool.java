package net.codekata.cloudkeytool;

import static java.util.function.Function.identity;

import io.atlassian.fugue.Option;
import io.atlassian.fugue.Unit;
import java.security.KeyStore.PasswordProtection;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Controller for KeyStore entity */
public final class CloudKeyTool implements Callable<CompletableFuture<Unit>> {
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
  public final CompletableFuture<Unit> call() {
    logger.info("Calling on {}.", service.getClass().getSimpleName());

    if (listEntries.isDefined()) {
      return listEntries();
    } else if (importKeyStore.isDefined()) {
      return importKeyStore();
    } else {
      logger.error("Nothing to do.");
      return CompletableFuture.failedFuture(new Exception("TODO: Define an exception type."));
    }
  }

  final CompletableFuture<Unit> listEntries() {
    /* a.k.a. doPrintEntries */
    final var ls = listEntries.get();
    return service
        .getKeyStore(ls.keystore, ls.storePass)
        .thenApplyAsync(
            ks -> {
              logger.info("Successfully loaded {}.", ls.keystore);
              final var repo = new KeyRepository(ks);
              return ls.dump(repo);
            });
  }

  final CompletableFuture<Unit> importKeyStore() {
    /* a.k.a. doimportKeyStore */
    final var is = importKeyStore.get();
    return service
        .getKeyStore(is.srcKeyStore, is.srcStorePass)
        .thenApplyAsync(
            ks -> {
              logger.info("Successfully loaded {}, {}.", is.srcKeyStore, ks.getType());
              return new KeyRepository(ks);
            })
        .thenCombineAsync(is.getDest(), is::update)
        .thenComposeAsync(
            ks ->
                new LocalKeyStoreService().store(ks.keyStore(), is.destKeyStore, is.destStorePass));
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

    private final Unit dump(KeyRepository repo) {
      repo.aliases().forEachRemaining(alias -> dump(repo, alias));
      return Utils.unit();
    }

    final void dump(KeyRepository repo, String alias) {
      final var keyPass = storePass;
      final var certs = repo.certificateChain(alias);
      final var priv = repo.privateKey(alias, keyPass);
      logger.info("Entry aliased {}.", alias);

      for (var cert : certs) {
        logger.info("Certificate of {}, {}.", cert.getPublicKey().getAlgorithm(), cert.getType());
      }

      // TODO: Report failure
      for (var p : priv) {
        logger.info("Private key of {}, {}.", p.getAlgorithm(), p.getFormat());
      }
    }
  }

  @Builder
  static final class ImportKeyStore {
    private final String srcKeyStore;
    private final PasswordProtection srcStorePass;
    private final String destKeyStore;
    private final PasswordProtection destStorePass;

    final KeyRepository update(KeyRepository src, KeyRepository dst) {
      // Assuming key passwods are the same as store pass.
      final var srcKeyPass = srcStorePass;
      final var destKeyPass = destStorePass;

      // According to keytool.html, "The destination entry will be protected
      // using destkeypass. If destkeypass is not provided, the destination
      // entry will be protected with the source entry password."
      // so always try to protect with destKeyPass.
      src.aliases()
          .forEachRemaining(
              alias -> {
                final var certs = src.certificateChain(alias);
                logger.info("Extracted certs from {}.", alias);
                src.privateKey(alias, srcKeyPass)
                    .flatMap(priv -> dst.store(alias, priv, destKeyPass, certs))
                    .map(
                        v -> {
                          logger.info("Successfully updated {} to {}.", alias, destKeyStore);
                          return v;
                        })
                    .fold(Utils::throwRuntime, identity());
              });
      return dst;
    }

    final CompletableFuture<KeyRepository> getDest() {
      return new LocalKeyStoreService()
          .getKeyStore(destKeyStore, destStorePass)
          .thenApplyAsync(KeyRepository::new);
    }
  }
}
