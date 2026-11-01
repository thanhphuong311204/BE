package com.datn.fashion;
import com.datn.fashion.entity.User;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.io.*;import java.nio.file.*;import java.net.*;import java.net.http.*;import java.time.Duration;import java.util.*;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="NOVA_TRYON_LIVE",matches="true")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"spring.datasource.url=jdbc:h2:mem:tryon_live;MODE=MySQL;DB_CLOSE_DELAY=-1","debug=false","logging.level.root=WARN","logging.level.org.springframework=WARN","logging.level.org.hibernate=WARN"})
@ActiveProfiles("test")
class BeautyLooksLiveTest {
 @LocalServerPort int port;@Autowired UserRepository users;@Autowired JwtService jwt;@Autowired ObjectMapper mapper;
 @DynamicPropertySource static void config(DynamicPropertyRegistry r){r.add("app.face-ai.url",()->System.getenv("FACE_AI_URL"));r.add("app.face-ai.key",()->System.getenv("FACE_AI_SERVICE_KEY"));}
 final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
 final Path root=Path.of("D:/DATN/FE"),output=root.resolve("docs/task_13_2");
 HttpResponse<String> request(String path,String token,byte[] image,String id,String intensity) throws Exception {
  var req=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).timeout(Duration.ofSeconds(30));
  if(token!=null)req.header("Authorization","Bearer "+token);
  if(image==null)return http.send(req.GET().build(),HttpResponse.BodyHandlers.ofString());
  String boundary="nova-tryon-boundary";var body=new ByteArrayOutputStream();
  for(var e:Map.of("recommendationId",id,"intensity",intensity,"color","#AD3456").entrySet())body.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+e.getKey()+"\"\r\n\r\n"+e.getValue()+"\r\n").getBytes());
  body.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes());body.write(image);body.write(("\r\n--"+boundary+"--\r\n").getBytes());
  return http.send(req.header("Content-Type","multipart/form-data; boundary="+boundary).POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
 }
 @Test void realHttpImageProcessingAndFlutter() throws Exception {
  var user=new User();user.email="tryon-live-"+UUID.randomUUID()+"@example.com";user.passwordHash="test-only";user=users.saveAndFlush(user);
  try {
   String token=jwt.create(user.id),path="/api/beauty/try-on";var rows=new ArrayList<Map<String,Object>>();
   byte[] source=Files.readAllBytes(root.resolve("assets/images/beauty/hair-side-fringe.jpg"));
   var caps=request(path+"/capabilities",token,null,"","");assertEquals(200,caps.statusCode());
   int enabled=0;for(var e:mapper.readTree(caps.body()).path("effects"))if(e.path("enabled").asBoolean())enabled++;assertEquals(13,enabled);
   rows.add(Map.of("check","real_capabilities_8_looks_5_legacy_hair_blocked","status","PASS"));
   var portraitResponse=http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path+"/portrait")).timeout(Duration.ofSeconds(30)).header("Authorization","Bearer "+token).header("Content-Type","multipart/form-data; boundary=nova-portrait").POST(HttpRequest.BodyPublishers.ofByteArray(portraitBody(source))).build(),HttpResponse.BodyHandlers.ofString());
   assertEquals(200,portraitResponse.statusCode());var portrait=mapper.readTree(portraitResponse.body());assertEquals("SUCCESS",portrait.path("status").asText());String portraitToken=portrait.path("portraitToken").asText();assertFalse(portraitToken.isBlank());assertEquals(4,portrait.path("faceBounds").size());
   rows.add(Map.of("check","portrait_prepared_once_with_face_roi","status","PASS"));
   var distinct=new HashSet<String>();
   for(String id:List.of("look-natural-daily","look-korean-soft-glam","look-douyin-inspired","look-clean-girl","look-peach","look-rose","look-soft-brown","look-evening-glam")) {
    var response=sessionRequest(path+"/session/render",token,portraitToken,id,"POST");assertEquals(200,response.statusCode());var data=mapper.readTree(response.body());assertEquals("SUCCESS",data.path("status").asText());
    distinct.add(data.path("imageBase64").asText());
    var original=ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(data.path("originalBase64").asText())));
    var edited=ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(data.path("imageBase64").asText())));
    assertNotNull(original);assertNotNull(edited);assertEquals(original.getWidth(),edited.getWidth());assertEquals(original.getHeight(),edited.getHeight());
    int changed=0;for(int y=0;y<original.getHeight();y++)for(int x=0;x<original.getWidth();x++)if(original.getRGB(x,y)!=edited.getRGB(x,y))changed++;assertTrue(changed>100);
    rows.add(Map.of("check",id,"status","PASS","changedPixels",changed,"processingTimeMs",data.path("processingTimeMs").asDouble()));
   }
   assertEquals(8,distinct.size());rows.add(Map.of("check","eight_distinct_real_outputs_without_reupload","status","PASS"));
   var other=new User();other.email="other-"+UUID.randomUUID()+"@example.com";other.passwordHash="test-only";other=users.saveAndFlush(other);
   try {assertEquals(410,sessionRequest(path+"/session/render",jwt.create(other.id),portraitToken,"look-natural-daily","POST").statusCode());}
   finally {users.deleteById(other.id);}
   assertEquals(204,sessionRequest(path+"/portrait",token,portraitToken,"look-natural-daily","DELETE").statusCode());
   assertEquals(410,sessionRequest(path+"/session/render",token,portraitToken,"look-natural-daily","POST").statusCode());
   rows.add(Map.of("check","session_owner_binding_release_and_expired_token","status","PASS"));
   var zero=mapper.readTree(request(path,token,source,"makeup-lip-focus","0").body());assertEquals(zero.path("imageBase64").asText(),zero.path("originalBase64").asText());rows.add(Map.of("check","intensity_zero_identity","status","PASS"));
   assertEquals(401,request(path,null,source,"makeup-lip-focus",".5").statusCode());assertEquals(401,request(path,"invalid",source,"makeup-lip-focus",".5").statusCode());
   rows.add(Map.of("check","JWT_missing_invalid","status","PASS"));
   assertEquals(400,request(path,token,new byte[]{1,2,3},"makeup-lip-focus",".5").statusCode());
   assertEquals(400,request(path,token,Files.readAllBytes(root.resolve("nova_face_ai/task_09_final_evaluation/fixtures/unsupported.bmp")),"makeup-lip-focus",".5").statusCode());
   assertEquals(413,request(path,token,new byte[5*1024*1024+1],"makeup-lip-focus",".5").statusCode());
   for(String id:List.of("hair-textured-short","makeup-soft-dimension","unknown"))assertEquals(400,request(path,token,source,id,".5").statusCode());
   assertEquals(400,request(path,token,source,"makeup-lip-focus","NaN").statusCode());rows.add(Map.of("check","invalid_oversize_unsupported_style_nan","status","PASS"));
   var noface=request(path,token,Files.readAllBytes(root.resolve("nova_face_ai/task_09_final_evaluation/fixtures/no_face.png")),"makeup-lip-focus",".5");assertEquals(200,noface.statusCode());assertEquals("NO_FACE",mapper.readTree(noface.body()).path("status").asText());rows.add(Map.of("check","NO_FACE","status","PASS"));
   var pic=ImageIO.read(new ByteArrayInputStream(source));var pair=new java.awt.image.BufferedImage(pic.getWidth()*2,pic.getHeight(),java.awt.image.BufferedImage.TYPE_INT_RGB);var graphics=pair.createGraphics();graphics.drawImage(pic,0,0,null);graphics.drawImage(pic,pic.getWidth(),0,null);graphics.dispose();var multi=new ByteArrayOutputStream();ImageIO.write(pair,"png",multi);
   var multiResponse=request(path,token,multi.toByteArray(),"makeup-lip-focus",".5");assertEquals(200,multiResponse.statusCode());assertEquals("MULTIPLE_FACES",mapper.readTree(multiResponse.body()).path("status").asText());rows.add(Map.of("check","MULTIPLE_FACES","status","PASS"));
   user.status=User.Status.INACTIVE;users.saveAndFlush(user);
   assertEquals(401,request(path+"/capabilities",token,null,"","").statusCode());user.status=User.Status.ACTIVE;users.saveAndFlush(user);rows.add(Map.of("check","inactive_account","status","PASS"));
   var child=new ProcessBuilder("cmd.exe","/c","D:\\flutter\\bin\\flutter.bat","test","test/beauty_studio_live_test.dart","--reporter","expanded")
    .directory(root.toFile()).redirectErrorStream(true).redirectOutput(output.resolve("flutter-live.log").toFile());
   child.environment().put("NOVA_TRYON_TEST_URL","http://127.0.0.1:"+port);child.environment().put("NOVA_TRYON_TEST_JWT",token);
   var process=child.start();if(!process.waitFor(150,java.util.concurrent.TimeUnit.SECONDS)){process.destroyForcibly();fail("Flutter live timeout");}assertEquals(0,process.exitValue(),"See docs/task_13_2/flutter-live.log");
   rows.add(Map.of("check","Flutter_HTTP_Spring_Python_before_after_strength_hair_gate","status","PASS"));
   mapper.writerWithDefaultPrettyPrinter().writeValue(output.resolve("TRYON_HTTP_RESULTS.json").toFile(),rows);
  }finally{users.deleteById(user.id);}
 }

 byte[] portraitBody(byte[] image)throws Exception {
   var out=new ByteArrayOutputStream();out.write("--nova-portrait\r\nContent-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n".getBytes());out.write(image);out.write("\r\n--nova-portrait--\r\n".getBytes());return out.toByteArray();
 }
 HttpResponse<String> sessionRequest(String path,String jwtToken,String portrait,String id,String method)throws Exception {
   String body=mapper.writeValueAsString(Map.of("portraitToken",portrait,"recommendationId",id,"intensity",.8,"color","#AD3456"));
   return http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).timeout(Duration.ofSeconds(30)).header("Authorization","Bearer "+jwtToken).header("Content-Type","application/json").method(method,HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
 }
}
