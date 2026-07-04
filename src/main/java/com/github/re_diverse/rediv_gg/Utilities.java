package com.github.re_diverse.rediv_gg;

public class Utilities
{
  public static boolean strNullCheck(String... values) {
    for (String str : values) {


      if (str == null) return false;


      if (str.trim().isEmpty()) return false;
    }
    return true;
  }

  public static boolean portCheck(int port) {
    if (port < 1) return false;
    if (port > 65535) return false;

    return true;
  }
}
