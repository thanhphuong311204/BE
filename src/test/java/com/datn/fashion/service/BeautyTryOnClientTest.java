package com.datn.fashion.service;
import com.datn.fashion.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import static org.junit.jupiter.api.Assertions.*;
class BeautyTryOnClientTest {
 static final String KEY="test-only-key-with-more-than-32-characters";
 ImageUploadValidator.ValidatedImage image(){return new ImageUploadValidator.ValidatedImage(new byte[]{1,2,3},"image.jpg");}
 void call(BeautyTryOnClient client){client.render(image(),"makeup-lip-focus",.5,"#AD3456",0,0,1);}
 @Test void missingConfigurationAndOfflineAre503() throws Exception {
  assertEquals(HttpStatus.SERVICE_UNAVAILABLE,assertThrows(ApiException.class,()->call(new BeautyTryOnClient("","",200))).status);
  int port;try(var socket=new java.net.ServerSocket(0)){port=socket.getLocalPort();}
  assertEquals(HttpStatus.SERVICE_UNAVAILABLE,assertThrows(ApiException.class,()->call(new BeautyTryOnClient("http://127.0.0.1:"+port,KEY,200))).status);
 }
 @Test void slowServiceIs504() throws Exception {
  var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/",ex->{try{ex.getRequestBody().readAllBytes();Thread.sleep(600);ex.sendResponseHeaders(503,-1);}catch(Exception ignored){}finally{ex.close();}});server.start();
  try{assertEquals(HttpStatus.GATEWAY_TIMEOUT,assertThrows(ApiException.class,()->call(new BeautyTryOnClient("http://127.0.0.1:"+server.getAddress().getPort(),KEY,100))).status);}finally{server.stop(0);}
 }
 @Test void fakeImagePayloadIsRejectedAndDtoToStringRedacts() throws Exception {
  var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/",ex->{ex.getRequestBody().readAllBytes();byte[] body="{\"status\":\"SUCCESS\",\"imageBase64\":\"fake\",\"originalBase64\":\"fake\",\"engineVersion\":\"test\",\"processingTimeMs\":1,\"warnings\":[]}".getBytes();ex.getResponseHeaders().set("Content-Type","application/json");ex.sendResponseHeaders(200,body.length);ex.getResponseBody().write(body);ex.close();});server.start();
  try{assertEquals(HttpStatus.BAD_GATEWAY,assertThrows(ApiException.class,()->call(new BeautyTryOnClient("http://127.0.0.1:"+server.getAddress().getPort(),KEY,1000))).status);}finally{server.stop(0);}
  var dto=new com.datn.fashion.dto.beauty.BeautyTryOnResponse("SUCCESS","private-payload","private-payload","test",1,java.util.List.of());
  assertFalse(dto.toString().contains("private-payload"));
 }
}
