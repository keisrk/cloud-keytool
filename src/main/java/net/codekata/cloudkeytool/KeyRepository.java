package net.codekata.cloudkeytool;

import static com.google.common.collect.Iterators.forEnumeration;
import static io.atlassian.fugue.Checked.now;

import java.security.KeyStore;
import java.util.Collections;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Wraper type around KeyStore. */
final class KeyRepository {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(KeyRepository.class);
  // OutputStream os;
  private final KeyStore ks;

  /** TODO: Use lombok's Builder. */
  KeyRepository(KeyStore ks) {
    this.ks = ks;
  }

  /** TODO: Use lombok. */
  KeyStore keyStore() {
    return ks;
  }

  Iterator<String> aliases() {
    final var aliases = now(() -> ks.aliases()).getOrElse(Collections::emptyEnumeration);
    return forEnumeration(aliases);
  }
}
