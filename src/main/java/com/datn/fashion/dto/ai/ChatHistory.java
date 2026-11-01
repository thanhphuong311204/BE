package com.datn.fashion.dto.ai;
import java.time.Instant;
import java.util.List;
public final class ChatHistory {
    private ChatHistory() {}
    public record Conversation(Long id, String title, Instant createdAt, Instant updatedAt) {}
    public record Message(Long id, Long conversationId, String role, String content, Instant createdAt, List<VisualBlock> blocks) {
        public Message {blocks=blocks==null?List.of():List.copyOf(blocks);}
        public Message(Long id,Long conversationId,String role,String content,Instant createdAt){this(id,conversationId,role,content,createdAt,List.of());}
        @Override public String toString() { return "Message[content=REDACTED]"; }
    }
    public record Conversations(List<Conversation> items, int page, int size, boolean hasMore) {}
    public record Messages(List<Message> items, Long nextBeforeId, boolean hasMore) {}
    public record Context(List<ChatRequest.Turn> history, Long lastMessageId) {}
}
