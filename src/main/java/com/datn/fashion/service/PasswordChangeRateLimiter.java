package com.datn.fashion.service;

import com.datn.fashion.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Bounded, per-account limit for a single backend instance; attempts include successes. */
@Component
public class PasswordChangeRateLimiter {
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_ACCOUNTS = 10_000;
    private record Attempts(Instant expiresAt, int count) {}
    private final Map<Long, Attempts> attempts = new HashMap<>();
    private final Clock clock;

    public PasswordChangeRateLimiter() { this(Clock.systemUTC()); }
    PasswordChangeRateLimiter(Clock clock) { this.clock = clock; }

    public synchronized void acquire(Long userId) {
        Instant now = clock.instant();
        attempts.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        Attempts previous = attempts.get(userId);
        if ((previous != null && previous.count() >= MAX_ATTEMPTS)
                || (previous == null && attempts.size() >= MAX_ACCOUNTS))
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "PASSWORD_CHANGE_RATE_LIMITED",
                "Bạn đã yêu cầu đổi mật khẩu quá nhiều lần. Vui lòng thử lại sau 15 phút.");
        attempts.put(userId, previous == null ? new Attempts(now.plus(WINDOW), 1)
            : new Attempts(previous.expiresAt(), previous.count() + 1));
    }
}
