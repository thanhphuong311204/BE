package com.datn.fashion.dto.ai;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
public record ChatResponse(String reply,String model,Long conversationId,Long messageId,Long userMessageId,List<VisualBlock> blocks) {
    public ChatResponse { blocks=blocks==null?List.of():List.copyOf(blocks); }
    public ChatResponse(String reply,String model,Long conversationId,Long messageId,Long userMessageId) {
        this(reply,model,conversationId,messageId,userMessageId,List.of());
    }
    @JsonProperty public int schemaVersion(){return 1;}
    @Override public String toString(){return "ChatResponse[content=REDACTED]";}
}
