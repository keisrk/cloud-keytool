package net.codekata.cloudkeytool;

import static com.google.common.collect.Iterators.forEnumeration;
import static io.atlassian.fugue.Checked.now;

import io.atlassian.fugue.Try;
import io.atlassian.fugue.Unit;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Wraper type around KeyStore. */
final class KeyRepository {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(KeyRepository.class);
  private final KeyStore ks;

  /** TODO: Use lombok's Builder. */
  KeyRepository(KeyStore ks) {
    this.ks = ks;
  }

  /** TODO: Use lombok. */
  final KeyStore keyStore() {
    return ks;
  }

  final Iterator<String> aliases() {
    final var aliases = now(() -> ks.aliases()).getOrElse(Collections::emptyEnumeration);
    return forEnumeration(aliases);
  }

  /** Certificate chain */
  final List<Certificate> certificateChain(String alias) {
    return now(() -> ks.getCertificateChain(alias))
        .map(Arrays::asList)
        .getOrElse(Collections::emptyList);
  }

  /** Private key */
  final Try<PrivateKey> privateKey(String alias, PasswordProtection keyPass) {
    return now(() -> ks.getEntry(alias, keyPass))
        .map(PrivateKeyEntry.class::cast)
        .map(PrivateKeyEntry::getPrivateKey);
  }

  final Try<Unit> store(
      String alias, PrivateKey priv, PasswordProtection keyPass, List<Certificate> certs) {
    return now(
        () -> {
          ks.setKeyEntry(alias, priv, keyPass.getPassword(), certs.toArray(Certificate[]::new));
          return Utils.unit();
        });
  }
}
