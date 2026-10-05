package com.datn.fashion.service;
import com.datn.fashion.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import java.net.http.HttpClient;
import java.time.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
@Service
public class CloudinaryAvatarStorage implements AvatarStorage {
 private final String cloud,key,secret;
 private final RestClient client;
 public CloudinaryAvatarStorage(@Value("${app.cloudinary.cloud-name}") String cloud,
 @Value("${app.cloudinary.api-key}") String key,@Value("${app.cloudinary.api-secret}") String secret) {
  this.cloud=cloud;this.key=key;this.secret=secret;
  var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
  factory.setReadTimeout(Duration.ofSeconds(30));
  client=RestClient.builder().requestFactory(factory).build();
 }
 @Override public UploadedAvatar upload(byte[] bytes,String filename,Long userId) {
  if(cloud.isBlank() || key.isBlank() || secret.isBlank() || !cloud.matches("[a-zA-Z0-9_-]+"))
   throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"AVATAR_NOT_CONFIGURED","Máy chủ chưa được cấu hình Cloudinary.");
  String publicId="fashion/avatars/"+userId+"/"+UUID.randomUUID();
  var params=new TreeMap<String,String>();params.put("public_id",publicId);params.put("timestamp",Long.toString(Instant.now().getEpochSecond()));
  var form=new LinkedMultiValueMap<String,Object>();params.forEach(form::add);
  form.add("api_key",key);form.add("signature",signature(params));
  form.add("file",new ByteArrayResource(bytes) { @Override public String getFilename() { return filename; } });
  try {
   Map<?,?> result=client.post().uri("https://api.cloudinary.com/v1_1/"+cloud+"/image/upload")
    .contentType(MediaType.MULTIPART_FORM_DATA).body(form).retrieve().body(Map.class);
   if(result==null || !(result.get("secure_url") instanceof String url) || !url.startsWith("https://res.cloudinary.com/") || !publicId.equals(result.get("public_id"))) throw new IllegalStateException();
   return new UploadedAvatar(url,publicId);
  } catch(Exception e) { throw new ApiException(HttpStatus.BAD_GATEWAY,"AVATAR_UPLOAD_FAILED","Không thể tải avatar lên. Vui lòng thử lại."); }
 }
 @Override public void delete(String publicId) {
  if(publicId==null || publicId.isBlank()) return;
  var params=new TreeMap<String,String>();params.put("public_id",publicId);params.put("timestamp",Long.toString(Instant.now().getEpochSecond()));
  var form=new LinkedMultiValueMap<String,Object>();params.forEach(form::add);form.add("api_key",key);form.add("signature",signature(params));
  try { client.post().uri("https://api.cloudinary.com/v1_1/"+cloud+"/image/destroy").contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().toBodilessEntity(); }
  catch(Exception e) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("Avatar cleanup failed; retry through maintenance."); }
 }
 private String signature(SortedMap<String,String> params) {
  String raw=params.entrySet().stream().map(e -> e.getKey()+"="+e.getValue()).collect(java.util.stream.Collectors.joining("&"))+secret;
  try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(raw.getBytes(StandardCharsets.UTF_8))); }
  catch(java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
 }
}
