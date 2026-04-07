package com.tencent.wxcloudrun.support;

import java.security.SecureRandom;
import java.time.Instant;

public final class Ids {

  private static final char[] CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
  private static final SecureRandom RANDOM = new SecureRandom();

  private Ids() {
  }

  public static String newId() {
    return encodeTime(Instant.now().toEpochMilli(), 10) + encodeRandom(16);
  }

  public static String newToken() {
    return newId() + encodeRandom(10);
  }

  private static String encodeTime(long value, int length) {
    char[] chars = new char[length];
    for (int i = length - 1; i >= 0; i--) {
      chars[i] = CROCKFORD[(int) (value & 31)];
      value >>>= 5;
    }
    return new String(chars);
  }

  private static String encodeRandom(int length) {
    char[] chars = new char[length];
    for (int i = 0; i < length; i++) {
      chars[i] = CROCKFORD[RANDOM.nextInt(CROCKFORD.length)];
    }
    return new String(chars);
  }
}
