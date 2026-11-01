package com.datn.fashion.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ChatRelevanceTest {
 @Test void oldMoneyMustMatchTheRequestedStyle() {
  assertTrue(ChatVisualCatalog.relevant("Tư vấn Old Money", "Polo ivory, quần tây", "old-money Old Money"));
  assertFalse(ChatVisualCatalog.relevant("Tư vấn Old Money", "Sơ mi và jeans", "minimalist"));
 }
 @Test void styleAloneDoesNotEstablishColorOrGarmentMatch() {
  assertFalse(ChatVisualCatalog.relevant("Minimalist sơ mi hồng", "Sơ mi trắng", "minimalist"));
  assertFalse(ChatVisualCatalog.relevant("Minimalist blazer kem", "Sơ mi kem", "minimalist"));
  assertTrue(ChatVisualCatalog.relevant("Minimalist sơ mi kem", "Sơ mi ivory", "minimalist"));
 }
 @Test void uncertainAndTextOnlyRequestsHaveNoPhotos() {
  assertFalse(ChatVisualCatalog.relevant("Phối màu trang phục", "Sơ mi ivory", "minimalist"));
  assertFalse(ChatVisualCatalog.relevant("Old Money không cần hình", "Polo ivory", "old money"));
  assertEquals(0,ChatVisualCatalog.requestedPhotos("Minimalist không cần ảnh"));
 }
}
