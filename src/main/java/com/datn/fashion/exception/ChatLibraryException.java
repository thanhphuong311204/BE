package com.datn.fashion.exception;

import com.datn.fashion.dto.ai.VisualBlock;
import java.util.List;

/** An AI error with optional editorial suggestions, never an assistant response. */
public class ChatLibraryException extends ApiException {
    public final List<VisualBlock> blocks;
    public ChatLibraryException(ApiException cause, List<VisualBlock> blocks) {
        super(cause.status, cause.code, cause.getMessage(), cause.fields);
        this.blocks = List.copyOf(blocks);
    }
}
