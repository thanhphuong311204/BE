package com.datn.fashion;
import com.datn.fashion.entity.*;
import com.datn.fashion.repository.*;
import com.datn.fashion.security.JwtService;
import com.datn.fashion.service.FaceInferenceClient;
import com.datn.fashion.dto.face.FaceAnalysisResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import java.util.*;
import java.io.*;
import javax.imageio.ImageIO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:faceprofile14;MODE=MySQL;DB_CLOSE_DELAY=-1","debug=false"})
@AutoConfigureMockMvc @ActiveProfiles("test")
class FaceProfileIntegrationTest {
 @Autowired MockMvc mvc;@Autowired UserRepository users;@Autowired UserProfileRepository profiles;@Autowired JwtService jwt;
 @MockitoBean FaceInferenceClient inference;
 @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
 User a,b;
 String base="/api/profile/face-shape";
 User make(){return new org.springframework.transaction.support.TransactionTemplate(transactions).execute(status->{var u=new User();u.email="face14-"+UUID.randomUUID()+"@example.com";u.passwordHash="test-only";u=users.saveAndFlush(u);var p=new UserProfile();p.user=u;p.fullName="Face test";profiles.saveAndFlush(p);return u;});}
 @BeforeEach void setup(){a=make();b=make();}
 @AfterEach void cleanup(){for(var u:List.of(a,b)){profiles.deleteById(u.id);users.deleteById(u.id);}}
 String token(User u){return "Bearer "+jwt.create(u.id);}
 String body(String shape,String previous){return "{\"confirmedFaceShape\":\""+shape+"\",\"confirmationSource\":\"MANUAL\",\"expectedFaceShape\":"+(previous==null?"null":"\""+previous+"\"")+"}";}
 @Test void saveReadIdempotenceChangeAndHistory()throws Exception {
  mvc.perform(get(base).header("Authorization",token(a))).andExpect(status().isOk()).andExpect(jsonPath("$.confirmedFaceShape").isEmpty());
  mvc.perform(put(base).header("Authorization",token(a)).contentType(MediaType.APPLICATION_JSON).content(body("OVAL",null))).andExpect(status().isOk()).andExpect(jsonPath("$.confirmedFaceShape").value("OVAL"));
  mvc.perform(get(base).header("Authorization",token(a))).andExpect(jsonPath("$.confirmedFaceShape").value("OVAL"));
  mvc.perform(put(base).header("Authorization",token(a)).contentType(MediaType.APPLICATION_JSON).content(body("OVAL",null))).andExpect(status().isOk());
  mvc.perform(get(base+"/history").header("Authorization",token(a))).andExpect(jsonPath("$.items.length()").value(1));
  mvc.perform(put(base).header("Authorization",token(a)).contentType(MediaType.APPLICATION_JSON).content(body("ROUND","OVAL"))).andExpect(status().isOk());
  mvc.perform(get(base+"/history").header("Authorization",token(a))).andExpect(jsonPath("$.items.length()").value(2)).andExpect(jsonPath("$.items[0].previousFaceShape").value("OVAL")).andExpect(jsonPath("$.items[0].confirmedFaceShape").value("ROUND"));
 }
 @Test void strictValidationAndStaleWrite()throws Exception {
  for(String shape:List.of("DIAMOND","oval",""))mvc.perform(put(base).header("Authorization",token(a)).contentType(MediaType.APPLICATION_JSON).content(body(shape,null))).andExpect(status().isBadRequest());
  mvc.perform(put(base).header("Authorization",token(a)).contentType(MediaType.APPLICATION_JSON).content("{\"confirmedFaceShape\":\"OVAL\",\"confirmationSource\":\"AI_PREDICTED\"}")).andExpect(status().isBadRequest());
  mvc.perform(put(base).header("Authorization",token(a)).contentType(MediaType.APPLICATION_JSON).content(body("OVAL",null))).andExpect(status().isOk());
  mvc.perform(put(base).header("Authorization",token(a)).contentType(MediaType.APPLICATION_JSON).content(body("SQUARE",null))).andExpect(status().isConflict());
  mvc.perform(get(base+"/history").header("Authorization",token(a))).andExpect(jsonPath("$.items.length()").value(1));
 }
 @Test void accountIsolationAndUnauthorized()throws Exception {
  mvc.perform(get(base)).andExpect(status().isUnauthorized());mvc.perform(get(base).header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
  mvc.perform(put(base).header("Authorization",token(a)).contentType(MediaType.APPLICATION_JSON).content(body("HEART",null))).andExpect(status().isOk());
  mvc.perform(get(base).header("Authorization",token(b))).andExpect(jsonPath("$.confirmedFaceShape").isEmpty());
  mvc.perform(get(base+"/history").header("Authorization",token(b))).andExpect(jsonPath("$.items.length()").value(0));
  mvc.perform(get(base+"/"+a.id).header("Authorization",token(b))).andExpect(status().isNotFound());
  mvc.perform(put(base).header("Authorization",token(b)).contentType(MediaType.APPLICATION_JSON).content("{\"confirmedFaceShape\":\"SQUARE\",\"confirmationSource\":\"MANUAL\",\"expectedUserId\":"+a.id+"}")).andExpect(status().isConflict());
  mvc.perform(get(base).header("Authorization",token(a))).andExpect(jsonPath("$.confirmedFaceShape").value("HEART"));
 }
 @Test void successfulInferenceNeverWritesConfirmedShape()throws Exception {
  mvc.perform(put(base).header("Authorization",token(a)).contentType(MediaType.APPLICATION_JSON).content(body("OVAL",null))).andExpect(status().isOk());
  when(inference.analyze(any())).thenReturn(new FaceAnalysisResponse(FaceAnalysisResponse.Status.SUCCESS,FaceAnalysisResponse.Shape.SQUARE,null,"explicit-ai-contract-stub",1.,List.of()));
  var pic=new java.awt.image.BufferedImage(100,100,java.awt.image.BufferedImage.TYPE_INT_RGB);var bytes=new ByteArrayOutputStream();ImageIO.write(pic,"png",bytes);
  mvc.perform(multipart("/api/ai/face-analysis").file(new MockMultipartFile("file","test.png","image/png",bytes.toByteArray())).header("Authorization",token(a))).andExpect(status().isOk()).andExpect(jsonPath("$.faceShape").value("SQUARE"));
  mvc.perform(get(base).header("Authorization",token(a))).andExpect(jsonPath("$.confirmedFaceShape").value("OVAL"));
  mvc.perform(get(base+"/history").header("Authorization",token(a))).andExpect(jsonPath("$.items.length()").value(1));
 }
}
