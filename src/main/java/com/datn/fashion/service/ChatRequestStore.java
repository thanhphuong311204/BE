package com.datn.fashion.service;

import com.datn.fashion.dto.ai.*;
import com.datn.fashion.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Durable reservation: concurrent requests never make a second provider call.
 * No lease takeover: an interrupted process leaves an ambiguous request pending.
 * A successful response is committed in the same transaction as both messages. */
@Service
public class ChatRequestStore {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    public ChatRequestStore(JdbcTemplate jdbc, ObjectMapper json) { this.jdbc=jdbc; this.json=json; }

    public ChatResponse reserve(Long user, ChatRequest request) {
        if (request.requestId()==null) return null; // Legacy clients retain their existing behavior.
        String fingerprint;
        try {
            fingerprint=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(
                (request.conversationId()+"\n"+request.message().trim()+(request.attachmentId()==null?"":"\nimage:"+request.attachmentId())).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
        try {
            jdbc.update("INSERT INTO chat_requests(user_id,request_id,fingerprint) VALUES (?,?,?)",
                user,request.requestId(),fingerprint);
            return null;
        } catch (DuplicateKeyException ex) {
            var rows=jdbc.queryForList("SELECT fingerprint,response FROM chat_requests WHERE user_id=? AND request_id=?",
                user,request.requestId());
            if (rows.isEmpty()) throw pending();
            var row=rows.getFirst();
            if (!fingerprint.equals(row.get("fingerprint")))
                throw new ApiException(HttpStatus.CONFLICT,"REQUEST_ID_REUSED","Mã yêu cầu đã được dùng cho nội dung khác.");
            if (row.get("response")==null) throw pending();
            try {
                var response=json.readValue(row.get("response").toString(),ChatResponse.class);
                var replies=jdbc.query("SELECT m.content,m.content_blocks FROM chat_messages m JOIN chat_conversations c ON c.id=m.conversation_id " +
                    "WHERE c.user_id=? AND c.id=? AND m.id=? AND m.role='ASSISTANT'",
                    (rs,i)->new ChatResponse(rs.getString(1),response.model(),response.conversationId(),response.messageId(),response.userMessageId(),StructuredChatContent.readBlocks(json,rs.getString(2))),user,response.conversationId(),response.messageId());
                if (replies.isEmpty())
                    throw new ApiException(HttpStatus.GONE,"CONVERSATION_DELETED","Cuộc trò chuyện đã bị xóa.");
                return replies.getFirst();
            } catch (java.io.IOException e) { throw new IllegalStateException("Invalid saved chat response",e); }
        }
    }
    private ApiException pending() {
        return new ApiException(HttpStatus.CONFLICT,"CHAT_REQUEST_PENDING",
            "NOVA vẫn đang xử lý yêu cầu này. Vui lòng chờ rồi thử lại.",Map.of("retryAfterSeconds","2"));
    }
    public void failed(Long user, String id) {
        if (id!=null) jdbc.update("DELETE FROM chat_requests WHERE user_id=? AND request_id=? AND response IS NULL",user,id);
    }
    // Called ONLY inside the history transaction.
    public void complete(Long user,String id,ChatResponse response) {
        if (id==null) return;
        try {
            if (jdbc.update("UPDATE chat_requests SET response=? WHERE user_id=? AND request_id=? AND response IS NULL",
                json.writeValueAsString(Map.of("model",response.model(),"conversationId",response.conversationId(),
                    "messageId",response.messageId(),"userMessageId",response.userMessageId())),user,id)!=1) throw new IllegalStateException("Missing chat reservation");
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
    }
}
