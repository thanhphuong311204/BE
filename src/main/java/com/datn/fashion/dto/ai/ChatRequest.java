package com.datn.fashion.dto.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record ChatRequest(
    @NotBlank @Size(max = 1000) String message,
    @Size(max = 12) List<@NotNull @Valid Turn> history,
    @Positive Long conversationId,
    @Pattern(regexp = "[a-zA-Z0-9-]{16,64}") String requestId,
    @Pattern(regexp = "[a-f0-9-]{36}") String attachmentId
) {
    public ChatRequest(String message,List<Turn> history,Long conversationId,String requestId) { this(message,history,conversationId,requestId,null); }
    public ChatRequest(String message, List<Turn> history) { this(message, history, null, null); }
    public ChatRequest(String message, List<Turn> history, Long conversationId) { this(message, history, conversationId, null); }
    @Override public String toString() { return "ChatRequest[content=REDACTED]"; }
    public record Turn(@NotNull Role role, @NotBlank @Size(max = 4000) String text) {}
    public enum Role { user, assistant }
}
