package com.datn.fashion.exception;
import org.springframework.http.HttpStatus;
import java.util.Map;
public class ApiException extends RuntimeException {
 public final HttpStatus status;
 public final String code;
 public final Map<String,String> fields;
 public ApiException(HttpStatus status,String code,String message) { this(status,code,message,Map.of()); }
 public ApiException(HttpStatus status,String code,String message,Map<String,String> fields) {
  super(message); this.status=status; this.code=code; this.fields=fields;
 }
}
