package com.datn.fashion.service;

import com.datn.fashion.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.security.MessageDigest;

/** Private local chat media. Never public URLs, wardrobe assets or provider File API. */
@Service @EnableScheduling
public class ChatImageStore {
    private final Path root;
    private final ImageUploadValidator validator;
    public ChatImageStore(@Value("${app.chat-images.directory:./data/chat-images}") String directory,ImageUploadValidator validator) {
        this.root=Path.of(directory).toAbsolutePath().normalize();this.validator=validator;
    }
    private Path image(Long owner,String id) {
        if(owner==null||owner<=0||id==null||!id.matches("[a-f0-9-]{36}"))throw missing();
        return root.resolve(owner.toString()).resolve(id+".jpg");
    }
    public synchronized String save(Long owner,String uploadId,MultipartFile file) {
        if(uploadId==null||!uploadId.matches("[a-zA-Z0-9-]{16,64}"))throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_UPLOAD_ID","Mã tải ảnh không hợp lệ.");
        var input=validator.validate(file);
        String id=UUID.nameUUIDFromBytes((owner+":"+uploadId).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        Path target=image(owner,id), digest=target.resolveSibling(id+".sha256");
        try {
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.bytes()));
            cleanup();
            if(Files.exists(target)&&Files.exists(digest)) {
                if(!Files.readString(digest).equals(hash))throw new ApiException(HttpStatus.CONFLICT,"UPLOAD_ID_REUSED","Mã tải ảnh đã dùng cho ảnh khác.");
                return id;
            }
            cleanup();Files.createDirectories(target.getParent());
            try(var files=Files.list(target.getParent())) {
                var all=files.filter(p->p.toString().endsWith(".jpg")).toList();
                if(all.size()>=100)throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,"CHAT_IMAGE_LIMIT","Tối đa 20 ảnh/ngày. Vui lòng xóa ảnh cũ hoặc thử lại sau.");
            }
            try(var files=Files.walk(root)) {
                long count=files.filter(p->p.toString().endsWith(".jpg")).count();
                if(count>=1000)throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"CHAT_IMAGE_CAPACITY","Kho ảnh chat tạm đầy. Vui lòng thử lại sau.");
            }
            Path quota=target.getParent().resolve("quota.txt");
            var timestamps=new ArrayList<String>();long today=System.currentTimeMillis()-86400000L;
            if(Files.exists(quota))for(String stamp:Files.readAllLines(quota))try{if(Long.parseLong(stamp)>today)timestamps.add(stamp);}catch(NumberFormatException ignored){}
            if(timestamps.size()>=20)throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,"CHAT_IMAGE_LIMIT","Tối đa 20 lượt tải ảnh trong 24 giờ. Vui lòng thử lại sau.");
            timestamps.add(Long.toString(System.currentTimeMillis()));Files.write(quota,timestamps);
            BufferedImage original=ImageIO.read(new ByteArrayInputStream(input.bytes()));
            double scale=Math.min(1,1280.0/Math.max(original.getWidth(),original.getHeight()));
            var normalized=new BufferedImage(Math.max(1,(int)(original.getWidth()*scale)),Math.max(1,(int)(original.getHeight()*scale)),BufferedImage.TYPE_INT_RGB);
            var graphics=normalized.createGraphics();graphics.setColor(java.awt.Color.WHITE);graphics.fillRect(0,0,normalized.getWidth(),normalized.getHeight());
            graphics.drawImage(original,0,0,normalized.getWidth(),normalized.getHeight(),null);graphics.dispose();
            var bytes=new ByteArrayOutputStream();if(!ImageIO.write(normalized,"jpg",bytes))throw new IOException();
            if(bytes.size()>5*1024*1024)throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE,"FILE_TOO_LARGE","Ảnh tối đa 5 MB.");
            Path temp=Files.createTempFile(target.getParent(),"pending-",".tmp");
            try {Files.write(temp,bytes.toByteArray());Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);Files.writeString(digest,hash);}
            finally{Files.deleteIfExists(temp);}
            return id;
        } catch(ApiException e){throw e;}
        catch(Exception e){throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"CHAT_IMAGE_STORAGE","Chưa lưu được ảnh. Vui lòng thử lại.");}
    }
    public byte[] read(Long owner,String id) {
        Path file=image(owner,id);
        try {
            if(!Files.isRegularFile(file)||Files.getLastModifiedTime(file).toMillis()<System.currentTimeMillis()-7L*86400000)throw missing();
            return Files.readAllBytes(file);
        }catch(IOException e){throw missing();}
    }
    public void remove(Long owner,String id) {
        Path file=image(owner,id);
        try{Files.deleteIfExists(file);Files.deleteIfExists(file.resolveSibling(id+".sha256"));}
        catch(IOException e){throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"CHAT_IMAGE_STORAGE","Chưa xóa được ảnh. Vui lòng thử lại.");}
    }
    @Scheduled(fixedDelay=3600000) public synchronized void cleanup() {
        if(!Files.isDirectory(root))return;
        try(var files=Files.walk(root)) {
            long cutoff=System.currentTimeMillis()-7L*86400000;
            for(Path p:files.filter(Files::isRegularFile).toList())if(Files.getLastModifiedTime(p).toMillis()<cutoff)Files.deleteIfExists(p);
        }catch(IOException ignored){/* No paths or image metadata in logs. Next cycle retries. */}
    }
    private ApiException missing(){return new ApiException(HttpStatus.NOT_FOUND,"CHAT_IMAGE_UNAVAILABLE","Ảnh không còn khả dụng hoặc đã hết hạn 7 ngày. Vui lòng gửi lại ảnh.");}
}
