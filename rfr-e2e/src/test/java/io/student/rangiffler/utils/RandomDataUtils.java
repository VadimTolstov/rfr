package io.student.rangiffler.utils;

import java.security.SecureRandom;

public final class RandomDataUtils {

  private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
  private static final SecureRandom RANDOM = new SecureRandom();

  private RandomDataUtils() {}

  public static String randomUsername() {
    StringBuilder sb = new StringBuilder("testuser_");
    for (int i = 0; i < 8; i++) {
      sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
    }
    return sb.toString();
  }

  public static String randomPassword() {
    return "TestPass123!";
  }
}
