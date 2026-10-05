package com.datn.fashion.service;
import java.util.Locale;
public final class Identifiers {
 private Identifiers() {}
 public static String email(String input) { return input.trim().toLowerCase(Locale.ROOT); }
 public static String phone(String input) { String p=input.trim(); return p.startsWith("+84") ? "0"+p.substring(3) : p; }
}
