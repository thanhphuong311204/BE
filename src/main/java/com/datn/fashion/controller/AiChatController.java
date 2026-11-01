package com.datn.fashion.controller;

import com.datn.fashion.dto.ai.*;
import com.datn.fashion.exception.ApiException;
import com.datn.fashion.service.AiChatService;
import com.datn.fashion.service.ChatHistoryStore;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {
    @org.springframework.beans.factory.annotation.Autowired private com.datn.fashion.service.ChatImageStore images;
    private final AiChatService service;
    private final ChatHistoryStore history;
    public AiChatController(AiChatService service, ChatHistoryStore history) { this.service = service; this.history = history; }

    @PostMapping("/chat")
    public ChatResponse chat(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChatRequest request) {
        org.slf4j.MDC.put("novaRequestId", request.requestId()==null ? java.util.UUID.randomUUID().toString() : request.requestId());
        try { return service.chat(userId(jwt), request); }
        finally { org.slf4j.MDC.remove("novaRequestId"); }
    }
    @PostMapping(value="/attachments",consumes="multipart/form-data")
    public java.util.Map<String,String> upload(@AuthenticationPrincipal Jwt jwt,@RequestPart("file") org.springframework.web.multipart.MultipartFile file,@RequestParam String uploadId) {
        Long owner=userId(jwt);service.account(owner);return java.util.Map.of("id",images.save(owner,uploadId,file));
    }
    @GetMapping("/attachments/{imageId}")
    public org.springframework.http.ResponseEntity<byte[]> image(@AuthenticationPrincipal Jwt jwt,@PathVariable String imageId) {
        Long owner=userId(jwt);service.account(owner);
        return org.springframework.http.ResponseEntity.ok().contentType(org.springframework.http.MediaType.IMAGE_JPEG)
            .header("Cache-Control","no-store, private").header("X-Content-Type-Options","nosniff").body(images.read(owner,imageId));
    }
    @DeleteMapping("/attachments/{imageId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@AuthenticationPrincipal Jwt jwt,@PathVariable String imageId) {
        Long owner=userId(jwt);service.account(owner);images.remove(owner,imageId);
    }
    @GetMapping("/conversations")
    public ChatHistory.Conversations list(@AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Long user = userId(jwt); service.account(user); return history.list(user, page, size);
    }
    @GetMapping("/conversations/{id}/messages")
    public ChatHistory.Messages messages(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
        @RequestParam(required = false) Long beforeId, @RequestParam(defaultValue = "50") int size) {
        Long user = userId(jwt); service.account(user); return history.messages(user, id, beforeId, size);
    }
    @DeleteMapping("/conversations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long user = userId(jwt); service.account(user);
        var attachments=new java.util.HashSet<String>();
        Long before=null;
        do {
            var page=history.messages(user,id,before,50);
            for(var m:page.items())for(var b:m.blocks())if(b.type().equals("user_image")&&b.images()!=null)for(var image:b.images())attachments.add(image.id());
            before=page.hasMore()?page.nextBeforeId():null;
        } while(before!=null);
        history.delete(user, id);
        for(String image:attachments)images.remove(user,image);
    }
    private Long userId(Jwt jwt) {
        long id;
        try {
            id = Long.parseLong(jwt.getSubject());
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Token không hợp lệ.");
        }
        return id;
    }
}
