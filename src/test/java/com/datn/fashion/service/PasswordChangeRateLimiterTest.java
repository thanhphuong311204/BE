package com.datn.fashion.service;

import com.datn.fashion.dto.auth.ChangePasswordRequest;
import com.datn.fashion.exception.ApiException;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class PasswordChangeRateLimiterTest {
    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-10-11T00:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }
    @Test void limitExpiresWithoutExtendingWindowOnRejectedRequests() {
        var clock = new MutableClock();
        var limiter = new PasswordChangeRateLimiter(clock);
        for (int i=0;i<5;i++) limiter.acquire(1L);
        clock.now = clock.now.plusSeconds(899);
        assertEquals(429, assertThrows(ApiException.class, () -> limiter.acquire(1L)).status.value());
        assertDoesNotThrow(() -> limiter.acquire(2L));
        clock.now = clock.now.plusSeconds(1);
        assertDoesNotThrow(() -> limiter.acquire(1L));
    }
    @Test void passwordRequestDoesNotExposeCredentialsInDiagnosticString() {
        assertEquals("ChangePasswordRequest[credentials=REDACTED]",
            new ChangePasswordRequest("old-secret", "new-secret", "new-secret").toString());
    }
}
