package com.datn.fashion.service;

import com.datn.fashion.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.mock.http.client.MockClientHttpRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class CloudinaryImageStorageTest {
    private final Logger logger = (Logger) LoggerFactory.getLogger(CloudinaryImageStorage.class);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private Level previousLevel;
    @BeforeEach void captureSafeLogs() {
        previousLevel = logger.getLevel(); logger.setLevel(Level.WARN);
        logs.start(); logger.addAppender(logs);
    }
    @AfterEach void releaseSafeLogs() {
        logger.detachAppender(logs); logs.stop(); logger.setLevel(previousLevel);
    }
    private String lastLog() { return logs.list.getLast().getFormattedMessage(); }
    private void assertNoSensitiveLogging() {
        for (var event : logs.list) {
            String text = event.getFormattedMessage();
            for (String sensitive : List.of("test-key", "test-secret", "test-cloud", "private provider details", "sensitive-signature"))
                assertFalse(text.contains(sensitive));
            assertNull(event.getThrowableProxy());
        }
    }
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final CloudinaryImageStorage storage = new CloudinaryImageStorage("test-cloud", "test-key", "test-secret", builder.build());

    @Test void sendsSignedUploadWithServerOwnedPublicId() {
        var publicId = new String[1];
        server.expect(requestTo("https://api.cloudinary.com/v1_1/test-cloud/image/upload"))
            .andExpect(method(HttpMethod.POST)).andExpect(request -> {
                String body = ((MockClientHttpRequest) request).getBodyAsString();
                var values = new HashMap<String,String>();
                for (String field : List.of("public_id", "timestamp", "overwrite", "asset_folder", "api_key", "signature")) {
                    int start = body.indexOf("name=\"" + field + "\"");
                    assertTrue(start >= 0, field);
                    int valueStart = body.indexOf("\r\n\r\n", start) + 4;
                    int valueEnd = body.indexOf("\r\n", valueStart);
                    values.put(field, body.substring(valueStart, valueEnd));
                }
                publicId[0] = values.get("public_id");
                assertTrue(publicId[0].startsWith("nova-atelier/styles/7/"));
                assertEquals("test-key", values.get("api_key"));
                assertEquals("false", values.get("overwrite"));
                assertEquals("nova-atelier/styles", values.get("asset_folder"));
                String signed = "asset_folder=nova-atelier/styles&overwrite=false&public_id=" + publicId[0] + "&timestamp=" + values.get("timestamp") + "test-secret";
                try {
                    assertEquals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(signed.getBytes(StandardCharsets.UTF_8))), values.get("signature"));
                } catch (Exception e) { throw new AssertionError(e); }
                assertFalse(body.contains("test-secret"));
            }).andRespond(request -> withSuccess("{\"secure_url\":\"https://res.cloudinary.com/test-cloud/image/upload/test.png\",\"public_id\":\"" + publicId[0] + "\"}", MediaType.APPLICATION_JSON).createResponse(request));
        var result = storage.upload(new byte[]{1,2}, "image.png", ImageUploadTarget.STYLE, 7L);
        assertEquals(publicId[0], result.publicId()); server.verify();
    }
    @Test void missingConfigurationReturns503WithoutHttpRequest() {
        var empty = new CloudinaryImageStorage("", "", "", builder.build());
        var e = assertThrows(ApiException.class, () -> empty.upload(new byte[]{1}, "image.png", ImageUploadTarget.AVATAR, 1L));
        assertEquals(503, e.status.value()); server.verify();
    }
    @Test void providerErrorReturnsSanitized502() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("private provider details"));
        var e = assertThrows(ApiException.class, () -> storage.upload(new byte[]{1}, "image.png", ImageUploadTarget.AVATAR, 1L));
        assertEquals("IMAGE_UPLOAD_FAILED", e.code); assertFalse(e.getMessage().contains("private")); server.verify();
    }
    @Test void rejectsUntrustedProviderUrl() {
        server.expect(anything()).andRespond(withSuccess("{\"secure_url\":\"https://evil.example/a.png\",\"public_id\":\"wrong\"}", MediaType.APPLICATION_JSON));
        assertThrows(ApiException.class, () -> storage.upload(new byte[]{1}, "image.png", ImageUploadTarget.AVATAR, 1L)); server.verify();
    }
    @Test void cleanupUsesSignedDestroy() {
        server.expect(requestTo("https://api.cloudinary.com/v1_1/test-cloud/image/destroy"))
            .andExpect(method(HttpMethod.POST)).andExpect(content().string(org.hamcrest.Matchers.containsString("signature=")))
            .andRespond(withSuccess("{\"result\":\"ok\"}", MediaType.APPLICATION_JSON));
        storage.delete("nova-atelier/users/avatars/1/example"); server.verify();
    }

    @Test void logsInvalidCloudNameWithoutEchoingProviderValues() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
            .body("{\"error\":{\"message\":\"Unknown cloud name test-cloud test-key test-secret sensitive-signature\"}}"));
        var e = assertThrows(ApiException.class, () -> storage.upload(new byte[]{1}, "image.png", ImageUploadTarget.AVATAR, 1L));
        assertEquals("IMAGE_UPLOAD_FAILED", e.code);
        assertNotNull(e.getCause());
        assertTrue(lastLog().contains("providerStatus=401"));
        assertTrue(lastLog().contains("reason=INVALID_CLOUD_NAME"));
        assertTrue(lastLog().contains("exceptionType=Unauthorized"));
        assertNoSensitiveLogging(); server.verify();
    }
    @Test void logsInvalidSignatureWithoutLoggingTheSignature() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
            .body("{\"error\":{\"message\":\"Invalid Signature sensitive-signature test-secret\"}}"));
        assertThrows(ApiException.class, () -> storage.upload(new byte[]{1}, "image.png", ImageUploadTarget.AVATAR, 1L));
        assertTrue(lastLog().contains("reason=INVALID_SIGNATURE")); assertNoSensitiveLogging(); server.verify();
    }
    @Test void logsFolderModeRejectionSeparately() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
            .body("{\"error\":{\"message\":\"asset_folder is not supported in fixed folder mode\"}}"));
        assertThrows(ApiException.class, () -> storage.upload(new byte[]{1}, "image.png", ImageUploadTarget.AVATAR, 1L));
        assertTrue(lastLog().contains("providerStatus=400"));
        assertTrue(lastLog().contains("reason=FOLDER_MODE_REJECTED")); assertNoSensitiveLogging(); server.verify();
    }
    @Test void logsNetworkTimeoutWithoutExceptionMessage() {
        server.expect(anything()).andRespond(request -> { throw new java.net.SocketTimeoutException("private provider details test-secret"); });
        assertThrows(ApiException.class, () -> storage.upload(new byte[]{1}, "image.png", ImageUploadTarget.AVATAR, 1L));
        assertTrue(lastLog().contains("providerStatus=0"));
        assertTrue(lastLog().contains("reason=NETWORK_TIMEOUT")); assertNoSensitiveLogging(); server.verify();
    }
    @Test void handlesNonJsonErrorWithoutLeakingBody() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.BAD_GATEWAY).body("private provider details test-secret"));
        assertThrows(ApiException.class, () -> storage.upload(new byte[]{1}, "image.png", ImageUploadTarget.AVATAR, 1L));
        assertTrue(lastLog().contains("reason=PROVIDER_UNAVAILABLE")); assertNoSensitiveLogging(); server.verify();
    }
    @Test void acceptsStringErrorAndClassifiesInvalidApiKey() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
            .body("{\"error\":\"Unknown API key test-key\"}"));
        assertThrows(ApiException.class, () -> storage.upload(new byte[]{1}, "image.png", ImageUploadTarget.AVATAR, 1L));
        assertTrue(lastLog().contains("reason=INVALID_API_KEY")); assertNoSensitiveLogging(); server.verify();
    }
}
