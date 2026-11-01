package io.student.rangiffler.utils;

import java.util.UUID;

public final class RandomDataUtils {
  private RandomDataUtils() {
  }

  public static String randomUsername() {
    return "rfr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
  }

  public static String randomPassword() {
    return "Pass" + UUID.randomUUID().toString().replace("-", "").substring(0, 6) + "!";
  }
}
