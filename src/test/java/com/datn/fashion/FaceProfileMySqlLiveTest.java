package com.datn.fashion;
import com.datn.fashion.entity.*;
import com.datn.fashion.repository.*;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.net.*;import java.net.http.*;import java.nio.file.*;import java.time.Duration;import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
@EnabledIfEnvironmentVariable(named="NOVA_FACEPROFILE_MYSQL",matches="true")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"spring.sql.init.mode=never","spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver","debug=false","logging.level.root=WARN","logging.level.org.springframework=WARN","logging.level.org.hibernate=WARN"})
@ActiveProfiles("test")
class FaceProfileMySqlLiveTest {
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",()->System.getenv("FACE_PROFILE_MYSQL_URL"));r.add("spring.datasource.username",()->System.getenv("FACE_PROFILE_MYSQL_USER"));r.add("spring.datasource.password",()->System.getenv("FACE_PROFILE_MYSQL_PASSWORD"));}
 @LocalServerPort int port;@Autowired UserRepository users;@Autowired UserProfileRepository profiles;@Autowired PasswordEncoder passwords;@Autowired PlatformTransactionManager transactions;@Autowired ObjectMapper mapper;
 final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
 final String password="FaceProfile14_Test!";
 User create(){return new TransactionTemplate(transactions).execute(status->{var u=new User();u.email="nova-face14-"+UUID.randomUUID()+"@example.com";u.passwordHash=passwords.encode(password);u=users.saveAndFlush(u);var p=new UserProfile();p.user=u;p.fullName="Face profile integration fixture";profiles.saveAndFlush(p);return u;});}
 HttpResponse<String> request(String method,String path,String token,Object body)throws Exception {
  var r=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).timeout(Duration.ofSeconds(15));if(token!=null)r.header("Authorization","Bearer "+token);
  if(body!=null)r.header("Content-Type","application/json");return http.send(r.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build(),HttpResponse.BodyHandlers.ofString());
 }
 String login(User user)throws Exception {var r=request("POST","/api/auth/login",null,Map.of("identifier",user.email,"password",password));assertEquals(200,r.statusCode());var token=mapper.readTree(r.body()).path("token").asText();assertFalse(token.isBlank());return token;}
 Map<String,Object> confirmation(User owner,String shape,String previous){var r=new HashMap<String,Object>();r.put("confirmedFaceShape",shape);r.put("confirmationSource","MANUAL");r.put("expectedFaceShape",previous);r.put("expectedUserId",owner.id);return r;}
 @Test void realMySqlAndRealHttpPersistenceAcrossLogins()throws Exception {
  var a=create();var b=create();var checks=new ArrayList<Map<String,String>>();String base="/api/profile/face-shape";
  try {
   assertEquals(401,request("GET",base,null,null).statusCode());checks.add(Map.of("check","missing_JWT","status","PASS"));
   String tokenA=login(a);var empty=request("GET",base,tokenA,null);assertEquals(200,empty.statusCode());assertTrue(mapper.readTree(empty.body()).path("confirmedFaceShape").isNull());
   assertEquals(200,request("PUT",base,tokenA,confirmation(a,"OVAL",null)).statusCode());assertEquals("OVAL",mapper.readTree(request("GET",base,tokenA,null).body()).path("confirmedFaceShape").asText());checks.add(Map.of("check","new_profile_save_read_OVAL_MySQL","status","PASS"));
   assertEquals(200,request("PUT",base,tokenA,confirmation(a,"OVAL",null)).statusCode());assertEquals(1,mapper.readTree(request("GET",base+"/history",tokenA,null).body()).path("items").size());checks.add(Map.of("check","idempotent_no_duplicate_history","status","PASS"));
   tokenA=login(a);assertEquals("OVAL",mapper.readTree(request("GET",base,tokenA,null).body()).path("confirmedFaceShape").asText());checks.add(Map.of("check","new_login_reads_persisted_OVAL","status","PASS"));
   String tokenB=login(b);assertTrue(mapper.readTree(request("GET",base,tokenB,null).body()).path("confirmedFaceShape").isNull());assertEquals(0,mapper.readTree(request("GET",base+"/history",tokenB,null).body()).path("items").size());
   assertEquals(409,request("PUT",base,tokenB,confirmation(a,"SQUARE",null)).statusCode());assertEquals("OVAL",mapper.readTree(request("GET",base,tokenA,null).body()).path("confirmedFaceShape").asText());checks.add(Map.of("check","account_switch_and_cross_owner_rejected","status","PASS"));
   assertEquals(200,request("PUT",base,tokenA,confirmation(a,"ROUND","OVAL")).statusCode());
   var history=mapper.readTree(request("GET",base+"/history",tokenA,null).body()).path("items");assertEquals(2,history.size());assertEquals("OVAL",history.get(0).path("previousFaceShape").asText());assertEquals("ROUND",history.get(0).path("confirmedFaceShape").asText());checks.add(Map.of("check","explicit_change_ROUND_and_transactional_history","status","PASS"));
   assertEquals(409,request("PUT",base,tokenA,confirmation(a,"HEART","OVAL")).statusCode());assertEquals(400,request("PUT",base,tokenA,confirmation(a,"DIAMOND","ROUND")).statusCode());checks.add(Map.of("check","stale_and_invalid_updates_rejected","status","PASS"));
   assertEquals(200,request("GET","/api/beauty/recommendations?faceShape=ROUND&type=MAKEUP&scope=RECOMMENDED",tokenA,null).statusCode());checks.add(Map.of("check","real_catalog_with_confirmed_ROUND","status","PASS"));
   var child=new ProcessBuilder("cmd.exe","/c","D:\\flutter\\bin\\flutter.bat","test","test/face_profile_live_test.dart","--reporter","expanded").directory(Path.of("D:/DATN/FE").toFile()).redirectErrorStream(true).redirectOutput(Path.of("D:/DATN/FE/docs/task_14/flutter-live.log").toFile());
   child.environment().put("NOVA_FACEPROFILE_TEST_URL","http://127.0.0.1:"+port);child.environment().put("NOVA_FACEPROFILE_TEST_JWT",tokenA);child.environment().put("NOVA_FACEPROFILE_TEST_OWNER",a.id.toString());
   var process=child.start();if(!process.waitFor(110,java.util.concurrent.TimeUnit.SECONDS)){process.destroyForcibly();fail("Flutter HTTP test timeout");}assertEquals(0,process.exitValue(),"See flutter-live.log");
   assertEquals("HEART",mapper.readTree(request("GET",base,login(a),null).body()).path("confirmedFaceShape").asText());
   checks.add(Map.of("check","Flutter_real_HTTP_manual_update_reopen_and_new_login","status","PASS"));
   var path=Path.of("D:/DATN/FE/docs/task_14/MYSQL_HTTP_RESULTS.json");mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(),checks);
  }finally{for(var u:List.of(a,b)){profiles.deleteById(u.id);users.deleteById(u.id);}}
 }
}
