package com.datn.fashion.dto.style;
import com.datn.fashion.entity.UserStylePreferences;
import java.time.Instant;
import java.util.List;
public record StylePreferencesResponse(
    UserStylePreferences.Status status,
    List<Long> styleIds, List<Long> colorIds, List<Long> occasionIds,
    Instant completedAt, Instant skippedAt
) {
    public static StylePreferencesResponse from(UserStylePreferences p) {
        return new StylePreferencesResponse(p.status,
            p.styleIds.stream().sorted().toList(), p.colorIds.stream().sorted().toList(),
            p.occasionIds.stream().sorted().toList(), p.completedAt, p.skippedAt);
    }
    public static StylePreferencesResponse notStarted() {
        return new StylePreferencesResponse(UserStylePreferences.Status.NOT_STARTED,
            List.of(), List.of(), List.of(), null, null);
    }
}
