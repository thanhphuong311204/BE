package com.datn.fashion.service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import com.datn.fashion.exception.ApiException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ChatImageStoreTest {
 @TempDir Path directory;
 private MockMultipartFile image() throws Exception {
  var output=new java.io.ByteArrayOutputStream();
  javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(20,30,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",output);
  return new MockMultipartFile("file","photo.png","image/png",output.toByteArray());
 }
 @Test void privateStorageNormalizesImageAndSurvivesRestart() throws Exception {
  var store=new ChatImageStore(directory.toString(),new ImageUploadValidator());
  String key=UUID.randomUUID().toString(), id=store.save(1L,key,image());
  assertEquals(id,store.save(1L,key,image()));
  var restarted=new ChatImageStore(directory.toString(),new ImageUploadValidator());
  assertEquals(id,restarted.save(1L,key,image()));
  byte[] bytes=restarted.read(1L,id);assertEquals(255,bytes[0]&255);assertEquals(216,bytes[1]&255);
  assertThrows(ApiException.class,()->restarted.read(2L,id));
  restarted.remove(2L,id);assertArrayEquals(bytes,restarted.read(1L,id));
  restarted.remove(1L,id);assertThrows(ApiException.class,()->restarted.read(1L,id));
 }
 @Test void sevenDayExpiryNeverReturnsOldImage() throws Exception {
  var store=new ChatImageStore(directory.toString(),new ImageUploadValidator());
  String id=store.save(1L,UUID.randomUUID().toString(),image());
  Path file=directory.resolve("1").resolve(id+".jpg");
  Files.setLastModifiedTime(file,FileTime.fromMillis(System.currentTimeMillis()-8L*86400000));
  assertThrows(ApiException.class,()->store.read(1L,id));store.cleanup();assertFalse(Files.exists(file));
 }
 @Test void deletingImagesAndRestartingDoesNotResetUploadLimit() throws Exception {
  var store=new ChatImageStore(directory.toString(),new ImageUploadValidator());
  for(int i=0;i<20;i++){String id=store.save(1L,UUID.randomUUID().toString(),image());store.remove(1L,id);}
  var restarted=new ChatImageStore(directory.toString(),new ImageUploadValidator());
  var error=assertThrows(ApiException.class,()->restarted.save(1L,UUID.randomUUID().toString(),image()));
  assertEquals(429,error.status.value());
 }
}
