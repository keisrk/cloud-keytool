package net.codekata.cloudkeytool.exceptions;

import java.io.IOException;
import java.security.GeneralSecurityException;

/** Custom checked exception */
public class KeyToolException extends Exception {
  private final Throwable cause;

  /** Wrap IO exception. */
  KeyToolException(IOException cause) {
    this.cause = cause;
  }

  /** Wrap exceptions related to various key store handlings. */
  KeyToolException(GeneralSecurityException cause) {
    this.cause = cause;
  }
}
