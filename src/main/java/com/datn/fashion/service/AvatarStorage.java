package com.datn.fashion.service;
public interface AvatarStorage {
 record UploadedAvatar(String secureUrl,String publicId) {}
 UploadedAvatar upload(byte[] bytes,String filename,Long userId);
 void delete(String publicId);
}
