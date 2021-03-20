package net.codekata.cloudkeytool;

import java.security.KeyStore.PasswordProtection;
import java.util.Optional;
import java.util.concurrent.Callable;
import net.codekata.cloudkeytool.CloudKeyTool.ImportKeyStore;
import net.codekata.cloudkeytool.CloudKeyTool.ListEntries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

@Command(
    name = "cloud-keytool",
    version = "0.0.1-SNAPSHOT",
    mixinStandardHelpOptions = true,
    description =
        "%nExtend keytool to store, manage and retrieve secrets from a cloud secret manager.%n",
    footer = "%nPlease report issues at https://github.com/keisrk/cloud-keytool/issues",
    commandListHeading = "%nProviders:%n%nThe most commonly used providers are:%n",
    synopsisSubcommandLabel = "PROVIDER",
    scope = ScopeType.INHERIT)
final class CloudKeyToolCommand implements Callable<CloudKeyToolModule> {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(CloudKeyToolCommand.class);

  @Option(names = "--list", description = "Lists entries in a keystore")
  private boolean list;

  @Option(
      names = "--importkeystore",
      description = "Imports one or all entries from another keystore")
  private boolean importKeyStore;

  @Option(names = "--keystore", paramLabel = "KEYSTORE", description = "The keystore location")
  private Optional<String> keystore;

  @Option(
      names = "--storepass",
      paramLabel = "STORE_PASSWD",
      description = "password protecting the keystore")
  private Optional<String> storePass;

  // --importkeystore from cloud services to local file system.
  @Option(names = "--srckeystore", paramLabel = "SRC_KEYSTORE", description = "The src keystore")
  private Optional<String> srcKeyStore;

  @Option(names = "--srcstorepass", paramLabel = "SRC_STORE_PASSWD", description = "FIXME")
  private Optional<String> srcStorePass;

  @Option(names = "--destkeystore", paramLabel = "DEST_KEYSTORE", description = "The dest keystore")
  private Optional<String> destKeyStore;

  @Option(names = "--deststorepass", paramLabel = "DEST_STORE_PASSWD", description = "FIXME")
  private Optional<String> destStorePass;

  private static final PasswordProtection password(Optional<String> passwd) {
    return passwd
        .map(p -> new PasswordProtection(p.toCharArray()))
        .orElseGet(
            () ->
                new PasswordProtection(
                    System.console().readPassword("[%s]", "Keystore password:")));
  }

  @Override
  public final CloudKeyToolModule call() throws Exception {
    if (list == importKeyStore) {
      throw new Exception("Must specify either --list or --importkeystore.");
    } else if (list) {
      final var ks = keystore.orElseThrow(() -> new Exception("No keystore provided"));
      final var pw = password(storePass);
      return new CloudKeyToolModule(new ListEntries(ks, pw));
    } else {
      // Handle the case of --importkeystore.
      final var importKeyStore =
          ImportKeyStore.builder()
              .srcKeyStore(srcKeyStore.orElseThrow(() -> new Exception("No src keystore")))
              .srcStorePass(password(srcStorePass))
              .destKeyStore(destKeyStore.orElseThrow(() -> new Exception("No dest keystore")))
              .destStorePass(password(destStorePass))
              .build();
      return new CloudKeyToolModule(importKeyStore);
    }
  }
}
