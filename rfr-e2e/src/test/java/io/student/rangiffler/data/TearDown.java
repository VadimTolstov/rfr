package io.student.rangiffler.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Runs registered cleanup actions and preserves failures from every action. */
public final class TearDown {
  private final List<Runnable> actions;

  public TearDown() {
    actions = List.of();
  }

  private TearDown(List<Runnable> actions) {
    this.actions = List.copyOf(actions);
  }

  public TearDown with(Runnable action) {
    List<Runnable> accumulated = new ArrayList<>(actions);
    accumulated.add(action);
    return new TearDown(accumulated);
  }

  public <T> TearDown withEach(Iterable<T> items, Consumer<? super T> action) {
    List<Runnable> accumulated = new ArrayList<>(actions);
    for (T item : items) {
      accumulated.add(() -> action.accept(item));
    }
    return new TearDown(accumulated);
  }

  public void close() {
    RuntimeException failure = null;
    for (Runnable action : actions) {
      try {
        action.run();
      } catch (RuntimeException exception) {
        if (failure == null) {
          failure = exception;
        } else {
          failure.addSuppressed(exception);
        }
      }
    }
    if (failure != null) {
      throw failure;
    }
  }
}
