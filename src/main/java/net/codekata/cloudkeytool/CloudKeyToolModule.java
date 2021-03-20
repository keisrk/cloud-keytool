package net.codekata.cloudkeytool;

import static io.atlassian.fugue.Option.none;
import static io.atlassian.fugue.Option.option;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.atlassian.fugue.Option;
import net.codekata.cloudkeytool.CloudKeyTool.ImportKeyStore;
import net.codekata.cloudkeytool.CloudKeyTool.ListEntries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** DI */
public final class CloudKeyToolModule extends AbstractModule {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(CloudKeyToolModule.class);

  private final Option<ListEntries> listEntries;
  private final Option<ImportKeyStore> importKeyStore;

  CloudKeyToolModule(ListEntries listEntries) {
    this.listEntries = option(listEntries);
    this.importKeyStore = none();
  }

  CloudKeyToolModule(ImportKeyStore importKeyStore) {
    this.listEntries = none();
    this.importKeyStore = option(importKeyStore);
  }

  @Provides
  public final Option<ListEntries> listEntries() {
    return listEntries;
  }

  @Provides
  public final Option<ImportKeyStore> importKeyStore() {
    return importKeyStore;
  }
}
