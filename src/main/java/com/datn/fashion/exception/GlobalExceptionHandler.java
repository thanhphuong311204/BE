package com.datn.fashion.exception;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import java.util.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(ApiException.class)
 ResponseEntity<ApiError> api(ApiException e) { return ResponseEntity.status(e.status).body(new ApiError(e.code,e.getMessage(),e.fields)); }
 @ExceptionHandler(MethodArgumentNotValidException.class)
 ResponseEntity<ApiError> validation(MethodArgumentNotValidException e) {
  var fields=new LinkedHashMap<String,String>();
  e.getBindingResult().getFieldErrors().forEach(err -> {
   String key=switch(err.getField()) { case "passwordMatching" -> "confirmPassword"; case "passwordWithinByteLimit" -> "password"; default -> err.getField(); };
   fields.putIfAbsent(key,err.getDefaultMessage());
  });
  return ResponseEntity.badRequest().body(new ApiError("VALIDATION_ERROR","Vui lòng kiểm tra dữ liệu.",fields));
 }
 @ExceptionHandler(DataIntegrityViolationException.class)
 ResponseEntity<ApiError> conflict() { return ResponseEntity.status(409).body(new ApiError("DUPLICATE_ACCOUNT","Email hoặc số điện thoại đã tồn tại.",Map.of())); }
 @ExceptionHandler(MaxUploadSizeExceededException.class)
 ResponseEntity<ApiError> large() { return ResponseEntity.status(413).body(new ApiError("FILE_TOO_LARGE","Ảnh cần nhỏ hơn hoặc bằng 5 MB.",Map.of())); }
 @ExceptionHandler({HttpMessageNotReadableException.class,MissingServletRequestPartException.class})
 ResponseEntity<ApiError> badRequest() { return ResponseEntity.badRequest().body(new ApiError("INVALID_REQUEST","Dữ liệu gửi lên không hợp lệ.",Map.of())); }
 @ExceptionHandler(Exception.class)
 ResponseEntity<ApiError> unexpected(Exception e) {
  // Do not log request bodies, tokens, passwords or provider responses.
  org.slf4j.LoggerFactory.getLogger(getClass()).error("Request failed: {}",e.getClass().getSimpleName());
  return ResponseEntity.status(500).body(new ApiError("SERVER_ERROR","Máy chủ đang gặp lỗi. Vui lòng thử lại.",Map.of()));
 }
}
