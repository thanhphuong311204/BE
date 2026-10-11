package com.datn.fashion.service;

import com.datn.fashion.dto.media.ImageUploadResponse;
import com.datn.fashion.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CloudinaryImageStorage {
    private static final Logger LOG = LoggerFactory.getLogger(CloudinaryImageStorage.class);
    private static final ObjectMapper ERROR_JSON = new ObjectMapper();
    private final String cloud, key, secret;
    private final RestClient client;
    private final boolean dynamicFolders;

    @org.springframework.beans.factory.annotation.Autowired
    public CloudinaryImageStorage(@Value("${app.cloudinary.cloud-name}") String cloud,
            @Value("${app.cloudinary.api-key}") String key,
            @Value("${app.cloudinary.api-secret}") String secret,
            @Value("${app.cloudinary.dynamic-folders:true}") boolean dynamicFolders, RestClient.Builder builder) {
        this.cloud = cloud; this.key = key; this.secret = secret; this.dynamicFolders = dynamicFolders;
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build());
        factory.setReadTimeout(Duration.ofSeconds(30));
        client = builder.requestFactory(factory).build();
        LOG.info("Cloudinary configuration: cloudNamePresent={}, cloudNameSyntaxValid={}, apiKeyPresent={}, apiSecretPresent={}, dynamicFolders={}",
            !cloud.isBlank(), cloud.matches("[a-zA-Z0-9_-]+"), !key.isBlank(), !secret.isBlank(), dynamicFolders);
    }

    // Package-private constructor for deterministic HTTP transport tests.
    CloudinaryImageStorage(String cloud, String key, String secret, RestClient client) {
        this.cloud = cloud; this.key = key; this.secret = secret; this.client = client; this.dynamicFolders = true;
    }

    public ImageUploadResponse upload(byte[] bytes, String filename, ImageUploadTarget target, Long userId) {
        configured();
        String publicId = target.folder() + "/" + userId + "/" + UUID.randomUUID();
        var params = parameters(publicId);
        // Signed server-owned paths, random IDs and no overwrite of existing library assets.
        params.put("overwrite", "false");
        if (dynamicFolders) params.put("asset_folder", target.folder());
        var form = form(params);
        form.add("file", new ByteArrayResource(bytes) {
            @Override public String getFilename() { return filename; }
        });
        try {
            Map<?, ?> result = client.post().uri(endpoint("upload"))
                .contentType(MediaType.MULTIPART_FORM_DATA).body(form).retrieve().body(Map.class);
            if (result == null || !(result.get("secure_url") instanceof String url)
                    || !validUrl(url) || !publicId.equals(result.get("public_id")))
                throw new IllegalStateException("Invalid provider response");
            return new ImageUploadResponse(url, publicId);
        } catch (Exception e) {
            // Provider exception messages/bodies can echo API keys and signatures.
            // Log only exception types, numeric status and an allowlisted reason.
            int providerStatus = e instanceof RestClientResponseException response
                ? response.getStatusCode().value() : 0;
            LOG.warn("Cloudinary upload failed: exceptionType={}, providerStatus={}, reason={}, dynamicFolders={}",
                e.getClass().getSimpleName(), providerStatus, safeReason(e), dynamicFolders);
            var failure = new ApiException(HttpStatus.BAD_GATEWAY, "IMAGE_UPLOAD_FAILED", "Không thể tải ảnh lên. Vui lòng thử lại.");
            failure.initCause(e); // Preserve the cause for debugging; never serialize/log the throwable.
            throw failure;
        }
    }

    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        configured();
        try {
            client.post().uri(endpoint("destroy")).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form(parameters(publicId))).retrieve().toBodilessEntity();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Image cleanup failed; retry through maintenance.");
        }
    }

    private String safeReason(Exception failure) {
        if (failure instanceof RestClientResponseException response) {
            // Inspect a bounded provider error privately. Never emit the raw content.
            byte[] body = response.getResponseBodyAsByteArray();
            if (body.length <= 8192) {
                try {
                    var error = ERROR_JSON.readTree(body).path("error");
                    String message = (error.isTextual() ? error.asText() : error.path("message").asText("")).toLowerCase(Locale.ROOT);
                    if (message.contains("unknown cloud") || message.contains("invalid cloud_name") || message.contains("invalid cloud name"))
                        return "INVALID_CLOUD_NAME";
                    if (message.contains("unknown api key") || message.contains("invalid api key")) return "INVALID_API_KEY";
                    if (message.contains("invalid signature")) return "INVALID_SIGNATURE";
                    if (message.contains("asset_folder") || message.contains("fixed folder") || message.contains("dynamic folder"))
                        return "FOLDER_MODE_REJECTED";
                    if (message.contains("stale request") || message.contains("timestamp")) return "TIMESTAMP_REJECTED";
                    if (message.contains("invalid credentials") || message.contains("authorization") || message.contains("authentication"))
                        return "AUTHENTICATION_REJECTED";
                } catch (Exception ignored) { /* Do not log the parser exception or provider body. */ }
            }
            return response.getStatusCode().is5xxServerError() ? "PROVIDER_UNAVAILABLE" : "PROVIDER_REJECTED";
        }
        if (failure instanceof ResourceAccessException) {
            Throwable cause = failure;
            // Bound traversal to avoid cyclic/non-standard cause chains.
            for (int depth = 0; cause != null && depth < 10; depth++, cause = cause.getCause()) {
                if (cause instanceof java.net.UnknownHostException) return "DNS_ERROR";
                if (cause instanceof java.net.http.HttpTimeoutException || cause instanceof java.net.SocketTimeoutException)
                    return "NETWORK_TIMEOUT";
                if (cause instanceof javax.net.ssl.SSLException) return "TLS_ERROR";
                if (cause instanceof java.net.ConnectException) return "CONNECTION_ERROR";
            }
            return "NETWORK_ERROR";
        }
        return failure instanceof IllegalStateException ? "UNEXPECTED_PROVIDER_RESPONSE" : "CLIENT_ERROR";
    }

    private void configured() {
        if (cloud.isBlank() || key.isBlank() || secret.isBlank() || !cloud.matches("[a-zA-Z0-9_-]+"))
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CLOUDINARY_NOT_CONFIGURED", "Máy chủ chưa được cấu hình Cloudinary.");
    }
    private String endpoint(String action) { return "https://api.cloudinary.com/v1_1/" + cloud + "/image/" + action; }
    private SortedMap<String, String> parameters(String publicId) {
        var params = new TreeMap<String, String>();
        params.put("public_id", publicId);
        params.put("timestamp", Long.toString(Instant.now().getEpochSecond()));
        return params;
    }
    private LinkedMultiValueMap<String, Object> form(SortedMap<String, String> params) {
        var form = new LinkedMultiValueMap<String, Object>();
        params.forEach(form::add);
        form.add("api_key", key);
        form.add("signature", signature(params));
        return form;
    }
    private String signature(SortedMap<String, String> params) {
        String raw = params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&")) + secret;
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private boolean validUrl(String url) {
        try {
            var uri = URI.create(url);
            return "https".equals(uri.getScheme()) && "res.cloudinary.com".equals(uri.getHost())
                && uri.getUserInfo() == null && uri.getPath().startsWith("/" + cloud + "/") && url.length() <= 1000;
        } catch (IllegalArgumentException e) { return false; }
    }
}
