package com.datn.fashion.dto.face;

import java.util.List;

public record FaceAnalysisResponse(Status status, Shape faceShape, Double confidence,
                                   String modelVersion, Double processingTimeMs, List<String> warnings) {
    public enum Status { SUCCESS, NO_FACE, MULTIPLE_FACES, LOW_QUALITY, INVALID_IMAGE, ERROR }
    public enum Shape { HEART, OBLONG, OVAL, ROUND, SQUARE }
    public boolean valid() {
        return status != null && confidence == null && modelVersion != null && !modelVersion.isBlank()
            && processingTimeMs != null && Double.isFinite(processingTimeMs) && processingTimeMs >= 0
            && warnings != null && warnings.stream().allMatch(x -> x != null)
            && ((status == Status.SUCCESS) == (faceShape != null));
    }
}
