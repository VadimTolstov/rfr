package io.student.rangiffler.data;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Leases an in-memory user without changing persistent test data. */
public final class StaticUserFixture implements UserFixture {
  private static final int POOL_SIZE = 3;
  private static final long WAIT_SECONDS = 5;
  private static final BlockingQueue<TestUser> USERS = new ArrayBlockingQueue<>(POOL_SIZE);
  static {
    for (int index = 0; index < POOL_SIZE; index++) {
      USERS.add(TestUser.unique());
    }
  }
  private final AtomicBoolean created = new AtomicBoolean();
  private final AtomicReference<TestUser> leased = new AtomicReference<>();

  @Override
  public TestUser user() {
    TestUser user = leased.get();
    if (user == null) {
      throw new IllegalStateException("Static user fixture has no leased user");
    }
    return user;
  }

  @Override
  public void create() {
    if (!created.compareAndSet(false, true)) {
      throw new IllegalStateException("Static user fixture can only be created once");
    }
    TestUser user;
    try {
      user = USERS.poll(WAIT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException exception) {
      created.set(false);
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for a static test user", exception);
    }
    if (user == null) {
      created.set(false);
      throw new IllegalStateException("No static test user became available within " + WAIT_SECONDS + " seconds");
    }
    leased.set(user);
  }

  @Override
  public void close() {
    TestUser user = leased.getAndSet(null);
    if (user != null && !USERS.offer(user)) {
      throw new IllegalStateException("Static user pool is full while returning a leased user");
    }
  }
}
