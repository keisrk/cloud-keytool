package net.codekata.cloudkeytool;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.atlassian.fugue.Option;
import java.security.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Controller tailored to KeyStore entity */
public class CloudKeyTool {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(CloudKeyTool.class);

  final void printEntries(KeyRepository repo, String storePass) {
    /* a.k.a. doPrintEntries */
    repo.aliases()
        .forEachRemaining(
            alias -> {
              KeyStore.ProtectionParameter protParam =
                  new KeyStore.PasswordProtection(storePass.toCharArray());

              // TODO: Rework this try block as a method.
              try {
                final var cert = repo.keyStore().getCertificate(alias);
                final var priv =
                    ((KeyStore.PrivateKeyEntry) repo.keyStore().getEntry(alias, protParam))
                        .getPrivateKey();
                logger.info("Found alias {}", alias);
                logger.info(
                    "Certificate of {}, {}", cert.getPublicKey().getAlgorithm(), cert.getType());
                logger.info("Private key of {}, {}", priv.getAlgorithm(), priv.getFormat());
              } catch (Exception e) {
                logger.error(e.getMessage());
              }
            });
  }

  final void importKeyStore() {
    /* a.k.a. doimportKeyStore */
  }

  /** Loads keystore. */
  final void run(Option<String> profile, Option<String> name, Option<String> pass) {
    final Injector injector = Guice.createInjector(new CloudKeyToolModule(profile.get()));

    final KeyStoreService service = injector.getInstance(KeyStoreService.class);
    final KeyStoreService service2 = new LocalKeyStoreService();
    final KeyStoreService service3 = new ResourceKeyStoreService();
    logger.info(
        "Services created {}, {}, {}",
        service.getClass().getSimpleName(),
        service2.getClass().getSimpleName(),
        service3.getClass().getSimpleName());

    final var keyName = name.get();
    logger.info(keyName);
    final var storePass = pass.get();
    logger.info(storePass);

    service
        .getKeyStore(keyName, storePass)
        .thenAccept(
            ks -> {
              logger.info("Fetched key store {}.", keyName);
              final var repo = new KeyRepository(ks);
              printEntries(repo, storePass);
            })
        .join();
  }
}
