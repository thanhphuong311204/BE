package com.datn.fashion.service;

import com.datn.fashion.dto.face.FaceAnalysisResponse;
import com.datn.fashion.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class FaceInferenceClient {
    private final String url, key;
    private final RestClient http;
    public FaceInferenceClient(@Value("${app.face-ai.url:}") String url,
                               @Value("${app.face-ai.key:}") String key,
                               @Value("${app.face-ai.timeout-ms:20000}") int timeoutMs) {
        this.url = url; this.key = key;
        var transport=HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(2))
            .followRedirects(HttpClient.Redirect.NEVER).build();
        var factory=new JdkClientHttpRequestFactory(transport);
        factory.setReadTimeout(Duration.ofMillis(Math.max(100,timeoutMs)));
        this.http=RestClient.builder().requestFactory(factory).build();
    }
    public FaceAnalysisResponse analyze(ImageUploadValidator.ValidatedImage image) {
        if (url.isBlank() || key.length()<32)
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"FACE_AI_NOT_CONFIGURED","Dịch vụ phân tích khuôn mặt chưa sẵn sàng.");
        URI endpoint;
        try {
            endpoint=URI.create(url.replaceAll("/+$","")+"/v1/face-analysis");
            if (!java.util.Set.of("http","https").contains(endpoint.getScheme()) || endpoint.getHost()==null || endpoint.getUserInfo()!=null)
                throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"FACE_AI_NOT_CONFIGURED","Dịch vụ phân tích khuôn mặt chưa sẵn sàng.");
        }
        var bytes=new ByteArrayResource(image.bytes()) {
            @Override public String getFilename() { return image.filename(); }
        };
        var partHeaders=new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType(image.filename().endsWith("jpg")?"image/jpeg":image.filename().endsWith("png")?"image/png":"image/webp"));
        var body=new LinkedMultiValueMap<String,Object>();
        body.add("file",new HttpEntity<>(bytes,partHeaders));
        try {
            var result=http.post().uri(endpoint).header("X-Nova-Face-Key",key)
                .contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(FaceAnalysisResponse.class);
            if (result==null || !result.valid()) throw invalid();
            if (result.status()==FaceAnalysisResponse.Status.ERROR)
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"FACE_AI_UNAVAILABLE","Phân tích khuôn mặt tạm thời không khả dụng. Vui lòng thử lại.");
            return result;
        } catch (ResourceAccessException e) {
            boolean timeout=false;
            for(Throwable cause=e;cause!=null;cause=cause.getCause())
                if(cause instanceof java.net.http.HttpTimeoutException || cause instanceof java.net.SocketTimeoutException) timeout=true;
            throw new ApiException(timeout?HttpStatus.GATEWAY_TIMEOUT:HttpStatus.SERVICE_UNAVAILABLE,
                timeout?"FACE_AI_TIMEOUT":"FACE_AI_UNAVAILABLE",timeout?"Phân tích quá thời gian chờ. Vui lòng thử lại.":"Không kết nối được dịch vụ phân tích khuôn mặt.");
        } catch (RestClientResponseException e) {
            if(e.getStatusCode().value()==504) throw new ApiException(HttpStatus.GATEWAY_TIMEOUT,"FACE_AI_TIMEOUT","Phân tích quá thời gian chờ. Vui lòng thử lại.");
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"FACE_AI_UNAVAILABLE","Dịch vụ phân tích khuôn mặt tạm thời không khả dụng.");
        } catch (RestClientException e) { throw invalid(); }
    }
    private ApiException invalid() { return new ApiException(HttpStatus.BAD_GATEWAY,"FACE_AI_INVALID_RESPONSE","Dịch vụ phân tích trả dữ liệu không hợp lệ."); }
}
