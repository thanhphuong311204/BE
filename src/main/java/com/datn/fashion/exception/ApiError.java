package com.datn.fashion.exception;
import java.time.Instant;
import java.util.Map;
public record ApiError(String code,String message,Map<String,String> fields,Instant timestamp) {
 public ApiError(String code,String message,Map<String,String> fields) { this(code,message,fields,Instant.now()); }
}
