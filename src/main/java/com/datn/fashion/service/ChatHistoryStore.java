package com.datn.fashion.service;

import com.datn.fashion.dto.ai.*;
import com.datn.fashion.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.sql.*;
import java.util.*;

@Service
public class ChatHistoryStore {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final ChatRequestStore requests;
    private final com.fasterxml.jackson.databind.ObjectMapper json=new com.fasterxml.jackson.databind.ObjectMapper();
    public ChatHistoryStore(JdbcTemplate jdbc, PlatformTransactionManager manager, ChatRequestStore requests) {
        this.jdbc = jdbc; this.tx = new TransactionTemplate(manager); this.requests = requests;
    }
    private ChatHistory.Conversation row(ResultSet r, int i) throws SQLException {
        return new ChatHistory.Conversation(r.getLong("id"), r.getString("title"),
            r.getTimestamp("created_at").toInstant(), r.getTimestamp("updated_at").toInstant());
    }
    private ChatHistory.Message message(ResultSet r, int i) throws SQLException {
        return new ChatHistory.Message(r.getLong("id"), r.getLong("conversation_id"), r.getString("role"),
            r.getString("content"), r.getTimestamp("created_at").toInstant(), StructuredChatContent.readBlocks(json,r.getString("content_blocks")));
    }
    private void owned(Long userId, Long id, boolean lock) {
        var rows = jdbc.query("SELECT id FROM chat_conversations WHERE user_id=? AND id=?" +
            (lock ? " FOR UPDATE" : ""), (r, i) -> r.getLong(1), userId, id);
        if (rows.isEmpty()) throw new ApiException(HttpStatus.NOT_FOUND, "CONVERSATION_NOT_FOUND",
            "Không tìm thấy cuộc trò chuyện.");
    }
    private void page(int page, int size) {
        if (page < 0 || page > 100000 || size < 1 || size > 50)
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "Phân trang không hợp lệ.");
    }
    public ChatHistory.Conversations list(Long userId, int page, int size) {
        page(page, size);
        var items = jdbc.query("SELECT id,title,created_at,updated_at FROM chat_conversations " +
            "WHERE user_id=? ORDER BY updated_at DESC,id DESC LIMIT ? OFFSET ?", this::row,
            userId, size + 1, (long) page * size);
        return new ChatHistory.Conversations(List.copyOf(items.subList(0, Math.min(size, items.size()))),
            page, size, items.size() > size);
    }
    public ChatHistory.Messages messages(Long userId, Long id, Long beforeId, int size) {
        page(0, size);
        if (beforeId != null && beforeId <= 0)
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "Mốc tin nhắn không hợp lệ.");
        return tx.execute(status -> {
            owned(userId, id, false);
            var rows = beforeId == null
                ? jdbc.query("SELECT * FROM chat_messages WHERE conversation_id=? ORDER BY id DESC LIMIT ?",
                    this::message, id, size + 1)
                : jdbc.query("SELECT * FROM chat_messages WHERE conversation_id=? AND id<? ORDER BY id DESC LIMIT ?",
                    this::message, id, beforeId, size + 1);
            boolean more = rows.size() > size;
            var items = new ArrayList<>(rows.subList(0, Math.min(size, rows.size())));
            Collections.reverse(items);
            return new ChatHistory.Messages(items, more ? items.getFirst().id() : null, more);
        });
    }
    public ChatHistory.Context context(Long userId, Long id) {
        if (id == null) return new ChatHistory.Context(List.of(), null);
        var page = messages(userId, id, null, 12);
        var turns=new ArrayList<ChatRequest.Turn>();
        for(var m:page.items()) {
            String text=m.content()+("ASSISTANT".equals(m.role())?StructuredChatContent.summary(m.blocks()):"");
            turns.add(new ChatRequest.Turn("USER".equals(m.role())?ChatRequest.Role.user:ChatRequest.Role.assistant,
                text.substring(0,Math.min(4000,text.length()))));
        }
        while(turns.size()>2 && turns.stream().mapToInt(t->t.text().length()).sum()>12000) {turns.removeFirst();turns.removeFirst();}
        return new ChatHistory.Context(List.copyOf(turns),page.items().isEmpty()?null:page.items().getLast().id());
    }
    /** Keep image exclusions separate from the bounded model context. */
    public Set<String> displayedImages(Long userId, Long id) {
        if(id==null)return Set.of();
        owned(userId,id,false);
        var urls=new HashSet<String>();
        jdbc.query("SELECT content_blocks FROM chat_messages WHERE conversation_id=? AND role='ASSISTANT'",
            (org.springframework.jdbc.core.RowCallbackHandler) row -> {
                for(var b:StructuredChatContent.readBlocks(json,row.getString(1))) {
                    if(b.outfits()!=null)for(var o:b.outfits())if(o.imageUrl()!=null)urls.add(o.imageUrl());
                    if(b.items()!=null)for(var i:b.items())if(i.imageUrl()!=null)urls.add(i.imageUrl());
                    if(b.images()!=null)for(var i:b.images())if(i.imageUrl()!=null)urls.add(i.imageUrl());
                }
            },id);
        return Set.copyOf(urls);
    }
    // Gemini runs outside this transaction. Both messages commit atomically.
    // The locked version check prevents stale context from two devices being interleaved.
    public ChatResponse append(Long userId, Long conversationId, Long expectedLastId,
                               String text, String reply, String model) {
        return append(userId, conversationId, expectedLastId, text, reply, model, null);
    }
    public ChatResponse append(Long userId, Long conversationId, Long expectedLastId,
                               String text, String reply, String model, String requestId) {
        return append(userId,conversationId,expectedLastId,text,reply,model,requestId,List.of());
    }
    public ChatResponse append(Long userId,Long conversationId,Long expectedLastId,String text,String reply,String model,String requestId,List<VisualBlock> blocks) {
        return append(userId,conversationId,expectedLastId,text,reply,model,requestId,blocks,List.of());
    }
    public ChatResponse append(Long userId,Long conversationId,Long expectedLastId,String text,String reply,String model,String requestId,List<VisualBlock> blocks,List<VisualBlock> userBlocks) {
        return tx.execute(status -> {
            Long id = conversationId;
            if (id == null) {
                String title = text.strip().replaceAll("\\s+", " ");
                title = title.substring(0, title.offsetByCodePoints(0, Math.min(80, title.codePointCount(0, title.length()))));
                id = insert("INSERT INTO chat_conversations(user_id,title) VALUES (?,?)", userId, title);
            } else {
                owned(userId, id, true);
                Long last = jdbc.queryForObject("SELECT MAX(id) FROM chat_messages WHERE conversation_id=?", Long.class, id);
                if (!Objects.equals(last, expectedLastId))
                    throw new ApiException(HttpStatus.CONFLICT, "CONVERSATION_CHANGED",
                        "Cuộc trò chuyện đã có tin nhắn mới trên thiết bị khác. Vui lòng mở lại từ lịch sử.");
            }
            String userStructured;
            try {userStructured=json.writeValueAsString(userBlocks);}catch(java.io.IOException ex){throw new IllegalStateException(ex);}
            Long userMessage = insert("INSERT INTO chat_messages(conversation_id,role,content,content_blocks) VALUES (?,?,?,?)", id, "USER", text.strip(),userStructured);
            String structured;
            try {structured=json.writeValueAsString(blocks);}
            catch(java.io.IOException ex){throw new IllegalStateException(ex);}
            Long assistant = insert("INSERT INTO chat_messages(conversation_id,role,content,content_blocks) VALUES (?,?,?,?)", id, "ASSISTANT", reply,structured);
            jdbc.update("UPDATE chat_conversations SET updated_at=CURRENT_TIMESTAMP(6) WHERE id=?", id);
            var result = new ChatResponse(reply, model, id, assistant, userMessage, blocks);
            requests.complete(userId, requestId, result);
            return result;
        });
    }
    private Long insert(String sql, Object... values) {
        var key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            return statement;
        }, key);
        return Objects.requireNonNull(key.getKey()).longValue();
    }
    public void delete(Long userId, Long id) {
        tx.executeWithoutResult(status -> {
            owned(userId, id, true);
            jdbc.update("DELETE FROM chat_conversations WHERE user_id=? AND id=?", userId, id);
        });
    }
}
