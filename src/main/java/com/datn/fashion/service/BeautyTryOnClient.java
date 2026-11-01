package com.datn.fashion.service;

import com.datn.fashion.dto.beauty.BeautyTryOnResponse;
import com.datn.fashion.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Component
public class BeautyTryOnClient {
    private final String url,key;private final RestClient http;
    public BeautyTryOnClient(@Value("${app.face-ai.url:}")String url,@Value("${app.face-ai.key:}")String key,@Value("${app.face-ai.timeout-ms:20000}")int timeout) {
        this.url=url;this.key=key;
        var transport=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).followRedirects(HttpClient.Redirect.NEVER).build();
        var factory=new JdkClientHttpRequestFactory(transport);factory.setReadTimeout(Duration.ofMillis(Math.max(timeout,100)));
        http=RestClient.builder().requestFactory(factory).build();
    }
    private void configured(){if(url.isBlank()||key.length()<32)throw unavailable();}
    public Map<?,?> capabilities(){
        configured();try{return http.get().uri(url.replaceAll("/+$","")+"/v1/beauty/capabilities").header("X-Nova-Face-Key",key).retrieve().body(Map.class);}
        catch(RestClientException e){throw unavailable();}
    }
    public BeautyTryOnResponse render(ImageUploadValidator.ValidatedImage image,String id,double intensity,String color,double x,double y,double scale) {
        configured();
        var uri=UriComponentsBuilder.fromUriString(url.replaceAll("/+$","")+"/v1/beauty/try-on")
            .queryParam("recommendationId",id).queryParam("intensity",intensity).queryParam("color",color)
            .queryParam("offsetX",x).queryParam("offsetY",y).queryParam("scale",scale).build().encode().toUri();
        var file=new ByteArrayResource(image.bytes()){@Override public String getFilename(){return image.filename();}};
        var body=new LinkedMultiValueMap<String,Object>();body.add("file",file);
        try {
            var result=http.post().uri(uri).header("X-Nova-Face-Key",key).contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(BeautyTryOnResponse.class);
            if(result==null||!result.valid())throw new ApiException(HttpStatus.BAD_GATEWAY,"TRYON_INVALID_RESPONSE","Dữ liệu thử màu không hợp lệ.");
            if("ERROR".equals(result.status()))throw unavailable();return result;
        }catch(ResourceAccessException e){
            boolean timeout=false;for(Throwable cause=e;cause!=null;cause=cause.getCause())if(cause instanceof java.net.http.HttpTimeoutException||cause instanceof java.net.SocketTimeoutException)timeout=true;
            if(timeout)throw new ApiException(HttpStatus.GATEWAY_TIMEOUT,"TRYON_TIMEOUT","Thử màu quá thời gian chờ. Vui lòng thử lại.");throw unavailable();}
        catch(RestClientResponseException e){if(e.getStatusCode().value()==400)throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_TRYON_INPUT","Ảnh hoặc tùy chọn thử không hợp lệ.");throw unavailable();}
        catch(RestClientException e){throw unavailable();}
    }
    private ApiException unavailable(){return new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"TRYON_UNAVAILABLE","Beauty Studio tạm thời chưa sẵn sàng. Vui lòng thử lại.");}
}
