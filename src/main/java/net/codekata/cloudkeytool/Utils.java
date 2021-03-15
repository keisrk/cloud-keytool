package net.codekata.cloudkeytool;

import static io.atlassian.fugue.Checked.now;

import io.atlassian.fugue.Option;
import io.atlassian.fugue.Try;
import io.atlassian.fugue.Unit;

public final class Utils {
  /** A sensible default for unit type provided by fugue */
  public static Unit unit() {
    return Unit.valueOf("VALUE");
  }

  /** Transforms the Option<A> into a Try<A>, mapping None to NoSuchElementException. */
  public static <A> Try<A> fromOption(Option<A> opt) {
    return now(opt::get);
  }

  /** Lift any exceptions to unchecked one. */
  public static <T> T throwRuntime(Exception cause) {
    throw new RuntimeException(cause);
  }
}
