package com.datn.fashion.service;

import com.datn.fashion.dto.ai.ChatRequest;
import com.datn.fashion.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.*;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Component
public class GeminiClient {
    private final String key;
    private final String model;
    private final ObjectMapper json;
    private final HttpClient http;
    private volatile java.time.Instant retryAt = java.time.Instant.EPOCH;
    private volatile int cooldownStatus = 429;
    private ApiException throttled(long seconds) {
        return new ApiException(cooldownStatus == 503 ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.TOO_MANY_REQUESTS,
            cooldownStatus == 503 ? "AI_PROVIDER_BUSY" : "AI_QUOTA_EXCEEDED",
            cooldownStatus == 503
                ? "Dịch vụ Gemini tạm thời quá tải. Vui lòng thử lại sau thời gian chờ."
                : "Đã đạt giới hạn yêu cầu Gemini. Vui lòng thử lại sau thời gian chờ.",
            Map.of("retryAfterSeconds", Long.toString(Math.max(1,seconds))));
    }
    static long retrySeconds(String value) {
        try { return Math.max(1,Long.parseLong(value)); }
        catch (Exception ignored) {
            try { return Math.max(1,java.time.Duration.between(java.time.Instant.now(),
                java.time.ZonedDateTime.parse(value,java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()).toSeconds()+1); }
            catch (Exception ex) { return 60; }
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    public GeminiClient(@Value("${app.gemini.api-key:}") String key,
                        @Value("${app.gemini.model:gemini-3.8-flash}") String model,
                        ObjectMapper json) {
        this(key, model, json, HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build());
    }

    GeminiClient(String key, String model, ObjectMapper json, HttpClient http) {
        this.key = key.trim();
        // Explicit free-tier text Flash models only. No paid-model fallback.
        if (!Set.of("gemini-3.8-flash", "gemini-3.1-flash-lite", "gemini-3.5-flash-lite").contains(model))
            throw new IllegalArgumentException("GEMINI_MODEL must be an approved Free Tier Flash model.");
        this.model = model;
        this.json = json;
        this.http = http;
        org.slf4j.LoggerFactory.getLogger(getClass()).info(
            "Gemini configuration: keyConfigured={}, model={}", !this.key.isBlank(), model);
    }

    public String model() { return model; }

    public String generate(String instruction, ChatRequest chat) {
        return generate(instruction,chat,null);
    }

    public String generate(String instruction, ChatRequest chat, byte[] image) {
        long started = System.nanoTime();
        if (key.isBlank()) throw error(HttpStatus.SERVICE_UNAVAILABLE, "AI_NOT_CONFIGURED",
            "NOVA chưa được cấu hình. Vui lòng thử lại sau.");
        if (retryAt.isAfter(java.time.Instant.now()))
            throw throttled(java.time.Duration.between(java.time.Instant.now(),retryAt).toSeconds()+1);
        var contents = new ArrayList<Map<String, Object>>();
        if (chat.history() != null) for (var turn : chat.history()) {
            contents.add(content(turn.role() == ChatRequest.Role.user ? "user" : "model", turn.text()));
        }
        if(image==null)contents.add(content("user", chat.message().trim()));
        else contents.add(Map.of("role","user","parts",List.of(
            Map.of("inlineData",Map.of("mimeType","image/jpeg","data",Base64.getEncoder().encodeToString(image))),
            Map.of("text",chat.message().trim()))));
        var originalStatus=new java.util.concurrent.atomic.AtomicInteger();
        int historyChars=chat.history()==null?0:chat.history().stream().mapToInt(t->t.text().length()).sum();
        org.slf4j.LoggerFactory.getLogger(getClass()).info(
            "nova stage=gemini_request historyTurns={} historyChars={} instructionChars={} messageChars={} contextChars={} responseType=json",
            contents.size()-1,historyChars,instruction.length(),chat.message().length(),instruction.length()+historyChars+chat.message().length());
        try {
            var generation = new HashMap<String,Object>();
            generation.put("maxOutputTokens",2048);
            generation.put("temperature",0.7);
            generation.put("responseMimeType","application/json");
            // Keep answer capacity; reduce reasoning overhead for conversational styling.
            if ("gemini-3.8-flash".equals(model))
                generation.put("thinkingConfig",Map.of("thinkingLevel","low"));
            var body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", instruction))),
                "contents", contents,
                "generationConfig", generation);
            var request = HttpRequest.newBuilder(URI.create(
                "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent"))
                .timeout(Duration.ofSeconds(15)).header("Content-Type", "application/json")
                .header("x-goog-api-key", key)
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
            var response = send(request,originalStatus);
            int status = response.statusCode();
            originalStatus.set(status);
            org.slf4j.LoggerFactory.getLogger(getClass()).info(
                "nova stage=gemini status={} durationMs={} historyTurns={} instructionChars={}",
                status, (System.nanoTime()-started)/1_000_000, contents.size()-1, instruction.length());
            // Never propagate provider response bodies: they may contain sensitive diagnostics.
            if (status < 200 || status >= 300) logProviderError(status, response.body());
            if (status == 429 || status == 503) {
                String header = response.headers()==null ? null : response.headers().firstValue("Retry-After").orElse(null);
                if (header == null) header = providerRetryDelay(response.body());
                if (status == 429 || header != null) {
                    long seconds = retrySeconds(header);
                    java.time.Instant until;
                    try { until = java.time.Instant.now().plusSeconds(seconds); }
                    catch (java.time.DateTimeException | ArithmeticException ex) { until = java.time.Instant.MAX; }
                    synchronized (this) { if (until.isAfter(retryAt)) { cooldownStatus=status; retryAt=until; } }
                    throw throttled(seconds);
                }
            }
            if (status == 503) throw error(HttpStatus.SERVICE_UNAVAILABLE, "AI_PROVIDER_BUSY",
                "Dịch vụ Gemini đang quá tải. Bạn vui lòng thử lại sau nhé.");
            if (status == 504) throw error(HttpStatus.GATEWAY_TIMEOUT, "AI_PROVIDER_TIMEOUT",
                "Dịch vụ AI đang phản hồi chậm. Bạn vui lòng thử lại nhé.");
            if (status == 401 || status == 403) throw error(HttpStatus.SERVICE_UNAVAILABLE, "AI_PROVIDER_AUTH",
                "NOVA chưa thể truy cập dịch vụ AI. Vui lòng thử lại sau.");
            if (status == 400 || status == 412) {
                if (providerKeyReason(response.body())) throw error(HttpStatus.SERVICE_UNAVAILABLE, "AI_PROVIDER_AUTH",
                    "Cấu hình xác thực Gemini không hợp lệ. Vui lòng liên hệ hỗ trợ.");
                throw error(HttpStatus.SERVICE_UNAVAILABLE, "AI_PROVIDER_CONFIG",
                    "Cấu hình yêu cầu Gemini chưa hợp lệ. Vui lòng liên hệ hỗ trợ.");
            }
            if (status == 404) throw error(HttpStatus.SERVICE_UNAVAILABLE, "AI_MODEL_UNAVAILABLE",
                "Model của NOVA hiện chưa khả dụng. Vui lòng thử lại sau.");
            if (status < 200 || status >= 300) throw error(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_ERROR",
                "Dịch vụ Gemini trả về lỗi. Bạn vui lòng thử lại sau nhé.");
            var root = json.readTree(response.body());
            org.slf4j.LoggerFactory.getLogger(getClass()).info("nova stage=gemini_usage thoughtsTokens={} outputTokens={}",
                root.path("usageMetadata").path("thoughtsTokenCount").asInt(),
                root.path("usageMetadata").path("candidatesTokenCount").asInt());
            var candidate = root.path("candidates").path(0);
            if (!"STOP".equals(candidate.path("finishReason").asText())) {
                org.slf4j.LoggerFactory.getLogger(getClass()).warn(
                    "nova stage=gemini_response finishReason={} blockReason={}",
                    diagnosticCode(candidate.path("finishReason").asText()),
                    diagnosticCode(root.path("promptFeedback").path("blockReason").asText()));
                throw error(HttpStatus.BAD_GATEWAY, "AI_INCOMPLETE_RESPONSE",
                    "NOVA chưa thể trả lời đầy đủ. Bạn thử hỏi ngắn hơn nhé.");
            }
            var reply = new StringBuilder();
            for (var part : candidate.path("content").path("parts")) {
                if (!part.path("thought").asBoolean(false) && part.hasNonNull("text"))
                    reply.append(part.path("text").asText());
            }
            if (reply.toString().isBlank()) throw error(HttpStatus.BAD_GATEWAY, "AI_EMPTY_RESPONSE",
                "NOVA chưa có phản hồi phù hợp. Bạn thử diễn đạt lại nhé.");
            return reply.toString().trim();
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("nova stage=gemini kind=parse_error originalStatus={} durationMs={}",
                originalStatus,(System.nanoTime()-started)/1_000_000);
            throw error(HttpStatus.BAD_GATEWAY, "AI_INVALID_RESPONSE",
                "Gemini trả về dữ liệu không hợp lệ. Bạn vui lòng thử lại nhé.");
        } catch (HttpTimeoutException ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("nova stage=gemini status=TIMEOUT originalStatus={} kind=timeout durationMs={}",
                originalStatus.get()==0?"unavailable":originalStatus.get(),(System.nanoTime()-started)/1_000_000);
            throw error(HttpStatus.GATEWAY_TIMEOUT, "AI_TIMEOUT", "NOVA phản hồi hơi lâu. Bạn vui lòng thử lại nhé.");
        } catch (InterruptedException ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("nova stage=gemini status=INTERRUPTED durationMs={}",
                (System.nanoTime()-started)/1_000_000);
            Thread.currentThread().interrupt();
            throw error(HttpStatus.SERVICE_UNAVAILABLE, "AI_INTERRUPTED", "Yêu cầu bị gián đoạn. Vui lòng thử lại.");
        } catch (IOException ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("nova stage=gemini status=IO_ERROR durationMs={}",
                (System.nanoTime()-started)/1_000_000);
            throw error(HttpStatus.BAD_GATEWAY, "AI_CONNECTION_ERROR", "Không thể kết nối dịch vụ AI. Vui lòng thử lại.");
        }
    }

    // RetryInfo is typed provider metadata; never log or return the raw body.
    private String providerRetryDelay(String body) {
        try {
            for (var detail : json.readTree(body).path("error").path("details")) {
                if (!"type.googleapis.com/google.rpc.RetryInfo".equals(detail.path("@type").asText())) continue;
                String delay = detail.path("retryDelay").asText();
                if (!delay.matches("[0-9]{1,9}(\\.[0-9]{1,9})?s")) continue;
                return new java.math.BigDecimal(delay.substring(0, delay.length()-1))
                    .setScale(0, java.math.RoundingMode.CEILING).max(java.math.BigDecimal.ONE).toPlainString();
            }
        } catch (IOException | RuntimeException ignored) { }
        return null;
    }

    private boolean providerKeyReason(String body) {
        try {
            for (var detail : json.readTree(body).path("error").path("details")) {
                if (Set.of("API_KEY_INVALID", "API_KEY_EXPIRED", "API_KEY_SERVICE_BLOCKED", "API_KEY_HTTP_REFERRER_BLOCKED", "API_KEY_IP_ADDRESS_BLOCKED").contains(detail.path("reason").asText())) return true;
            }
        } catch (IOException | RuntimeException ignored) { }
        return false;
    }

    private static String diagnosticCode(String value) {
        return value != null && value.matches("[A-Z][A-Z0-9_]{0,63}") ? value : "UNKNOWN";
    }

    private void logProviderError(int status, String body) {
        int providerCode = 0;
        String providerStatus = "UNKNOWN";
        boolean highDemand = false;
        try {
            var detail = json.readTree(body).path("error");
            providerCode = detail.path("code").asInt();
            providerStatus = diagnosticCode(detail.path("status").asText());
            highDemand = detail.path("message").asText().toLowerCase(Locale.ROOT).contains("high demand");
        } catch (IOException | RuntimeException ignored) { /* Never log raw provider data. */ }
        org.slf4j.LoggerFactory.getLogger(getClass()).warn(
            "nova stage=gemini_error httpStatus={} providerCode={} providerStatus={} highDemand={}",
            status, providerCode, providerStatus, highDemand);
    }


    // HttpRequest timeout alone does not give a separately enforced full-body deadline.
    // Await the asynchronous full response and cancel the exchange on timeout/interruption.
    private HttpResponse<String> send(HttpRequest request,java.util.concurrent.atomic.AtomicInteger originalStatus) throws IOException, InterruptedException {
        long started=System.nanoTime();
        String trace=org.slf4j.MDC.get("novaRequestId");
        var future=http.sendAsync(request, info -> {
            originalStatus.set(info.statusCode());
            org.slf4j.LoggerFactory.getLogger(getClass()).info("nova requestId={} stage=gemini_headers status={} durationMs={}",
                trace,info.statusCode(),(System.nanoTime()-started)/1_000_000);
            return HttpResponse.BodyHandlers.ofString().apply(info);
        });
        try { return future.get(15,java.util.concurrent.TimeUnit.SECONDS); }
        catch (java.util.concurrent.TimeoutException ex) {
            future.cancel(true);
            throw new HttpTimeoutException("Gemini full response deadline exceeded");
        } catch (java.util.concurrent.ExecutionException ex) {
            if (ex.getCause() instanceof IOException io) throw io;
            throw new IOException("Gemini exchange failed",ex.getCause());
        } catch (InterruptedException ex) {
            future.cancel(true);
            throw ex;
        }
    }

    private Map<String, Object> content(String role, String text) {
        return Map.of("role", role, "parts", List.of(Map.of("text", text)));
    }
    private ApiException error(HttpStatus status, String code, String message) {
        return new ApiException(status, code, message);
    }
}
