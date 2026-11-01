package com.datn.fashion.service;

import com.datn.fashion.exception.ApiException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class FaceInferenceClientTest {
    HttpServer server;String url;String key="test-only-key-at-least-thirty-two-characters";
    @BeforeEach void start() throws Exception {
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.setExecutor(Executors.newCachedThreadPool(r->{var t=new Thread(r);t.setDaemon(true);return t;}));
        url="http://127.0.0.1:"+server.getAddress().getPort();server.start();
    }
    @AfterEach void stop(){server.stop(0);}
    ImageUploadValidator.ValidatedImage image(){return new ImageUploadValidator.ValidatedImage(new byte[]{1,2,3},"image.jpg");}
    @Test void forwardsMultipartAndPrivateKey() {
        server.createContext("/v1/face-analysis",exchange->{
            assertEquals(key,exchange.getRequestHeaders().getFirst("X-Nova-Face-Key"));
            assertTrue(exchange.getRequestHeaders().getFirst("Content-Type").startsWith("multipart/form-data"));
            assertTrue(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.ISO_8859_1).contains("name=\"file\""));
            var body="{\"status\":\"NO_FACE\",\"faceShape\":null,\"confidence\":null,\"modelVersion\":\"test\",\"processingTimeMs\":1,\"warnings\":[]}".getBytes();
            exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,body.length);exchange.getResponseBody().write(body);exchange.close();
        });
        assertEquals("NO_FACE",new FaceInferenceClient(url,key,2000).analyze(image()).status().name());
    }
    @Test void serviceDownAndTimeoutAreNormalized() {
        server.createContext("/v1/face-analysis",exchange->{try{Thread.sleep(500);}catch(InterruptedException e){Thread.currentThread().interrupt();}exchange.close();});
        assertEquals("FACE_AI_TIMEOUT",assertThrows(ApiException.class,()->new FaceInferenceClient(url,key,100).analyze(image())).code);
        server.stop(0);
        assertEquals("FACE_AI_UNAVAILABLE",assertThrows(ApiException.class,()->new FaceInferenceClient(url,key,100).analyze(image())).code);
    }
    @Test void rejectsUncalibratedNumericConfidence() {
        server.createContext("/v1/face-analysis",exchange->{
            var body="{\"status\":\"SUCCESS\",\"faceShape\":\"HEART\",\"confidence\":0.99,\"modelVersion\":\"test\",\"processingTimeMs\":1,\"warnings\":[]}".getBytes();
            exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,body.length);exchange.getResponseBody().write(body);exchange.close();
        });
        assertEquals("FACE_AI_INVALID_RESPONSE",assertThrows(ApiException.class,()->new FaceInferenceClient(url,key,2000).analyze(image())).code);
    }
}
