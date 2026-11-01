package com.datn.fashion.service;

import com.datn.fashion.dto.ai.ChatRequest;
import com.datn.fashion.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.net.http.*;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class GeminiClientTest {
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient http = mock(HttpClient.class);
    private final ChatRequest chat = new ChatRequest("Phối đồ đi học", List.of(
        new ChatRequest.Turn(ChatRequest.Role.user, "Tôi thích màu kem"),
        new ChatRequest.Turn(ChatRequest.Role.assistant, "Bạn thích phong cách nào?")));

    @SuppressWarnings("unchecked")
    private void response(int status, String body) throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        when(http.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"gemini-3.8-flash", "gemini-3.1-flash-lite", "gemini-3.5-flash-lite"})
    void officialEndpointHeaderHistoryAndTextOnly(String model) throws Exception {
        response(200, """
            {"candidates":[{"finishReason":"STOP","content":{"parts":[
            {"thought":true,"text":"private reasoning"},{"text":"Áo sơ mi kem và quần jeans."}]}}]}
            """);
        var client = new GeminiClient("test-placeholder", model, json, http);
        assertEquals("Áo sơ mi kem và quần jeans.", client.generate("system", chat));
        var captor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(http).sendAsync(captor.capture(), any(HttpResponse.BodyHandler.class));
        var sent = captor.getValue();
        assertEquals("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent", sent.uri().toString());
        assertFalse(sent.uri().toString().contains("test-placeholder"));
        assertEquals("test-placeholder", sent.headers().firstValue("x-goog-api-key").orElseThrow());
        assertEquals(15, sent.timeout().orElseThrow().toSeconds());
        var bytes = new java.io.ByteArrayOutputStream();
        sent.bodyPublisher().orElseThrow().subscribe(new java.util.concurrent.Flow.Subscriber<java.nio.ByteBuffer>() {
            public void onSubscribe(java.util.concurrent.Flow.Subscription s) { s.request(Long.MAX_VALUE); }
            public void onNext(java.nio.ByteBuffer b) { byte[] a = new byte[b.remaining()]; b.get(a); bytes.writeBytes(a); }
            public void onError(Throwable t) { fail("Cannot read request"); }
            public void onComplete() {}
        });
        var payload = json.readTree(bytes.toByteArray());
        if ("gemini-3.8-flash".equals(model))
            assertEquals("low",payload.path("generationConfig").path("thinkingConfig").path("thinkingLevel").asText());
        else assertFalse(payload.path("generationConfig").has("thinkingConfig"));
        assertEquals(2048,payload.path("generationConfig").path("maxOutputTokens").asInt());
        assertEquals("application/json",payload.path("generationConfig").path("responseMimeType").asText());
        assertFalse(payload.path("generationConfig").has("responseJsonSchema"));
        assertEquals(3, payload.path("contents").size());
        assertEquals("model", payload.path("contents").path(1).path("role").asText());
        assertEquals(chat.message(), payload.path("contents").path(2).path("parts").path(0).path("text").asText());
        assertEquals("system", payload.path("systemInstruction").path("parts").path(0).path("text").asText());
    }

    @Test void runtimeModelSendsInlineJpegWithHistoryAndUnchangedDeadline() throws Exception {
        response(200,"{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"parts\":[{\"text\":\"Analysis\"}]}}]}");
        var client=new GeminiClient("test-placeholder","gemini-3.1-flash-lite",json,http);
        client.generate("system",chat,new byte[]{1,2,3});
        var captor=org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(http).sendAsync(captor.capture(),any(HttpResponse.BodyHandler.class));
        var sent=captor.getValue();assertEquals(15,sent.timeout().orElseThrow().toSeconds());
        assertTrue(sent.uri().getPath().endsWith("gemini-3.1-flash-lite:generateContent"));
        var bytes=new java.io.ByteArrayOutputStream();
        sent.bodyPublisher().orElseThrow().subscribe(new java.util.concurrent.Flow.Subscriber<java.nio.ByteBuffer>() {
            public void onSubscribe(java.util.concurrent.Flow.Subscription s){s.request(Long.MAX_VALUE);}
            public void onNext(java.nio.ByteBuffer b){byte[] a=new byte[b.remaining()];b.get(a);bytes.writeBytes(a);}
            public void onError(Throwable t){fail(t);}public void onComplete(){}
        });
        var payload=json.readTree(bytes.toByteArray());
        assertEquals("image/jpeg",payload.path("contents").get(2).path("parts").get(0).path("inlineData").path("mimeType").asText());
        assertEquals("AQID",payload.path("contents").get(2).path("parts").get(0).path("inlineData").path("data").asText());
        assertEquals(chat.history().getFirst().text(),payload.path("contents").get(0).path("parts").get(0).path("text").asText());
    }
    @Test void mapsProviderErrorsWithoutLeakingBody() throws Exception {
        for (int status : new int[]{429, 401, 403, 404, 500, 503}) {
            response(status, "provider secret diagnostic");
            var ex = assertThrows(ApiException.class, () ->
                new GeminiClient("test-placeholder", "gemini-3.8-flash", json, http).generate("system", chat));
            assertEquals(status == 429 ? 429 : status == 401 || status == 403 || status == 404 || status == 503 ? 503 : 502, ex.status.value());
            assertFalse(ex.getMessage().contains("provider secret"));
        }
    }

    @Test void providerOverloadWithoutRetryAfterRemains503AndDoesNotInventCooldown() throws Exception {
        response(503,"{\"error\":{\"code\":503,\"status\":\"UNAVAILABLE\",\"message\":\"high demand secret diagnostic\"}}");
        var client=new GeminiClient("test-placeholder","gemini-3.8-flash",json,http);
        var ex=assertThrows(ApiException.class,()->client.generate("system",chat));
        assertEquals(503,ex.status.value());
        assertEquals("AI_PROVIDER_BUSY",ex.code);
        assertTrue(ex.fields.isEmpty());
        assertFalse(ex.getMessage().contains("secret"));
        response(200,"{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"parts\":[{\"text\":\"Chào bạn!\"}]}}]}");
        assertEquals("Chào bạn!",client.generate("system",chat));
        verify(http,times(2)).sendAsync(any(HttpRequest.class),any(HttpResponse.BodyHandler.class));
    }

    @Test void invalidKey400IsAuthenticationNotOverload() throws Exception {
        response(400, """
            {"error":{"status":"INVALID_ARGUMENT","message":"private diagnostic",
            "details":[{"@type":"type.googleapis.com/google.rpc.ErrorInfo","reason":"API_KEY_INVALID"}]}}
            """);
        var ex=assertThrows(ApiException.class,()->new GeminiClient("test-placeholder","gemini-3.8-flash",json,http).generate("system",chat));
        assertEquals("AI_PROVIDER_AUTH",ex.code);
        assertFalse(ex.getMessage().contains("private"));
    }

    @Test void invalidPayloadIsConfigurationNotBusy() throws Exception {
        response(400,"""
            {"error":{"status":"INVALID_ARGUMENT","message":"private diagnostic"}}
            """);
        var ex=assertThrows(ApiException.class,()->new GeminiClient("test-placeholder","gemini-3.8-flash",json,http).generate("system",chat));
        assertEquals("AI_PROVIDER_CONFIG",ex.code);
        assertEquals(503,ex.status.value());
    }

    @Test void typedRetryInfoIsRespectedWithoutRetryAfterHeader() throws Exception {
        response(503,"""
            {"error":{"status":"UNAVAILABLE","details":[
            {"@type":"type.googleapis.com/google.rpc.RetryInfo","retryDelay":"2.5s"}]}}
            """);
        var client=new GeminiClient("test-placeholder","gemini-3.8-flash",json,http);
        var first=assertThrows(ApiException.class,()->client.generate("system",chat));
        assertEquals("AI_PROVIDER_BUSY",first.code);
        assertEquals("3",first.fields.get("retryAfterSeconds"));
        assertThrows(ApiException.class,()->client.generate("system",chat));
        verify(http,times(1)).sendAsync(any(HttpRequest.class),any(HttpResponse.BodyHandler.class));
    }

    @Test void malformedProviderEnvelopeIsNotMisreportedAsNetworkFailure() throws Exception {
        response(200,"not-json");
        var client=new GeminiClient("test-placeholder","gemini-3.8-flash",json,http);
        var ex=assertThrows(ApiException.class,()->client.generate("system",chat));
        assertEquals(502,ex.status.value());
        assertEquals("AI_INVALID_RESPONSE",ex.code);
    }

    @Test void upstream504PreservesStatusAndDoesNotRetry() throws Exception {
        response(504,"sensitive diagnostic");
        var client=new GeminiClient("test-placeholder","gemini-3.8-flash",json,http);
        var ex=assertThrows(ApiException.class,()->client.generate("system",chat));
        assertEquals(504,ex.status.value());
        assertEquals("AI_PROVIDER_TIMEOUT",ex.code);
        verify(http,times(1)).sendAsync(any(HttpRequest.class),any(HttpResponse.BodyHandler.class));
    }
    @Test void timeoutAndNetworkErrorsAreExplicit() throws Exception {
        when(http.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new HttpTimeoutException("sensitive")));
        var client = new GeminiClient("test-placeholder", "gemini-3.8-flash", json, http);
        assertEquals("AI_TIMEOUT", assertThrows(ApiException.class, () -> client.generate("system", chat)).code);
        when(http.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new IOException("sensitive")));
        assertEquals("AI_CONNECTION_ERROR", assertThrows(ApiException.class, () -> client.generate("system", chat)).code);
    }

    @Test void missingKeyNeverMakesNetworkRequestAndPaidModelRejected() {
        var client = new GeminiClient("", "gemini-3.8-flash", json, http);
        assertEquals("AI_NOT_CONFIGURED", assertThrows(ApiException.class, () -> client.generate("system", chat)).code);
        verifyNoInteractions(http);
        assertThrows(IllegalArgumentException.class, () -> new GeminiClient("test-placeholder", "gemini-pro", json, http));
    }

    @Test void refusesEmptyBlockedTruncatedAndMalformedResponse() throws Exception {
        for (String body : List.of("{}", "not-json",
                "{\"candidates\":[{\"finishReason\":\"MAX_TOKENS\"}]}",
                "{\"candidates\":[{\"finishReason\":\"SAFETY\"}]}",
                "{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"parts\":[]}}]}")) {
            response(200, body);
            assertThrows(ApiException.class, () ->
                new GeminiClient("test-placeholder", "gemini-3.8-flash", json, http).generate("system", chat));
        }
    }

    @Test void retryAfterStopsRepeatedProviderCallsAndAcceptsHttpDates() throws Exception {
        for (int status : new int[]{429,503}) {
            reset(http);
            response(status,"never expose diagnostics");
            @SuppressWarnings("unchecked")
            HttpResponse<String> reply=(HttpResponse<String>)http.sendAsync(HttpRequest.newBuilder(java.net.URI.create("https://example.invalid")).build(),HttpResponse.BodyHandlers.ofString()).get();
            clearInvocations(http);
            when(reply.headers()).thenReturn(HttpHeaders.of(java.util.Map.of("Retry-After",List.of("120")),(a,b)->true));
            var client=new GeminiClient("test-placeholder","gemini-3.8-flash",json,http);
            var first=assertThrows(ApiException.class,()->client.generate("system",chat));
            assertEquals(status,first.status.value());
            assertEquals("120",first.fields.get("retryAfterSeconds"));
            var next=assertThrows(ApiException.class,()->client.generate("system",chat));
            assertTrue(Long.parseLong(next.fields.get("retryAfterSeconds"))>=119);
            verify(http,times(1)).sendAsync(any(HttpRequest.class),any(HttpResponse.BodyHandler.class));
        }
        assertEquals(60,GeminiClient.retrySeconds(null));
        assertEquals(60,GeminiClient.retrySeconds("invalid"));
        assertEquals(1,GeminiClient.retrySeconds("0"));
        var date=java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).plusSeconds(90)
            .format(java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME);
        assertTrue(GeminiClient.retrySeconds(date)>=89);
    }

    @Test void fullBodyDeadlineCancelsEvenWhenTransportNeverFinishes() throws Exception {
        var pending=new java.util.concurrent.CompletableFuture<HttpResponse<String>>();
        when(http.sendAsync(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenReturn(pending);
        var client=new GeminiClient("test-placeholder","gemini-3.8-flash",json,http);
        long start=System.nanoTime();
        assertEquals("AI_TIMEOUT",assertThrows(ApiException.class,()->client.generate("system",chat)).code);
        assertTrue(pending.isCancelled());
        long elapsed=(System.nanoTime()-start)/1_000_000;
        assertTrue(elapsed>=14900 && elapsed<18000,"Expected a bounded 15s total response deadline, got "+elapsed);
    }
}
