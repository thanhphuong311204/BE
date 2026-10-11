package com.datn.fashion;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.service.AvatarStorage;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import java.time.Instant;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class AuthProfileIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @Autowired UserRepository users;
 @org.springframework.test.context.bean.override.mockito.MockitoSpyBean com.datn.fashion.repository.UserProfileRepository profiles;
 @Autowired PasswordEncoder passwords;
 @Autowired JwtEncoder encoder;
 @MockitoBean AvatarStorage avatars;
 @BeforeEach void resetDatabase() { profiles.deleteAll();users.deleteAll(); }
 private String request(Object body) throws Exception { return json.writeValueAsString(body); }
 private Map<String,String> registration(String email,String phone) { return Map.of("fullName","Nguyễn Mai","email",email,"phoneNumber",phone,"password","123456","confirmPassword","123456"); }
 private JsonNode register(String email,String phone) throws Exception {
  return json.readTree(mvc.perform(post("/api/auth/register").contentType("application/json").content(request(registration(email,phone))))
   .andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.passwordHash").doesNotExist()).andReturn().getResponse().getContentAsString());
 }
 private String login(String identifier,String password) throws Exception {
  return json.readTree(mvc.perform(post("/api/auth/login").contentType("application/json").content(request(Map.of("identifier",identifier,"password",password))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.user.passwordHash").doesNotExist()).andReturn().getResponse().getContentAsString()).get("token").asText();
 }
 private org.springframework.test.web.servlet.ResultActions changePassword(String token, String current, String next, String confirmation) throws Exception {
  return mvc.perform(post("/api/auth/change-password").header("Authorization","Bearer "+token)
   .contentType("application/json").content(request(Map.of("currentPassword",current,"newPassword",next,"confirmPassword",confirmation))));
 }
 @Test void changePasswordRequiresJwt() throws Exception {
  var body=request(Map.of("currentPassword","123456","newPassword","new-password","confirmPassword","new-password"));
  mvc.perform(post("/api/auth/change-password").contentType("application/json").content(body)).andExpect(status().isUnauthorized());
  changePassword("invalid","123456","new-password","new-password").andExpect(status().isUnauthorized());
  var claims=JwtClaimsSet.builder().issuer("fashion-api").subject("1").issuedAt(Instant.now().minusSeconds(500)).expiresAt(Instant.now().minusSeconds(120)).build();
  String expired=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
  changePassword(expired,"123456","new-password","new-password").andExpect(status().isUnauthorized());
 }
 @Test void changePasswordHashesNewPasswordAndPreservesProfileAndJwt() throws Exception {
  register("mai@example.com","0901234567");register("other@example.com","0911234567");
  String token=login("mai@example.com","123456");
  changePassword(token,"123456","new-password","new-password").andExpect(status().isNoContent()).andExpect(content().string(""));
  var user=users.findByEmail("mai@example.com").orElseThrow();
  assertTrue(passwords.matches("new-password",user.passwordHash));assertFalse(passwords.matches("123456",user.passwordHash));
  assertTrue(passwords.matches("123456",users.findByEmail("other@example.com").orElseThrow().passwordHash));
  mvc.perform(post("/api/auth/login").contentType("application/json").content(request(Map.of("identifier","mai@example.com","password","123456"))))
   .andExpect(status().isUnauthorized());
  assertFalse(login("mai@example.com","new-password").isBlank());
  mvc.perform(get("/api/profile").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("mai@example.com"));
 }
 @Test void wrongCurrentPasswordDoesNotExpireSessionOrUpdateHash() throws Exception {
  register("mai@example.com","0901234567");String token=login("mai@example.com","123456");
  changePassword(token,"wrong-password","new-password","new-password").andExpect(status().isBadRequest())
   .andExpect(jsonPath("$.code").value("CURRENT_PASSWORD_INCORRECT")).andExpect(jsonPath("$.fields.currentPassword").exists());
  assertTrue(passwords.matches("123456",users.findByEmail("mai@example.com").orElseThrow().passwordHash));
  mvc.perform(get("/api/profile").header("Authorization","Bearer "+token)).andExpect(status().isOk());
 }
 @Test void changePasswordValidatesConfirmationLengthAndUtf8Bytes() throws Exception {
  register("mai@example.com","0901234567");String token=login("mai@example.com","123456");
  changePassword(token,"123456","new-password","mismatch").andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.confirmPassword").exists());
  changePassword(token,"123456","short","short").andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.newPassword").exists());
  changePassword(token,"123456","é".repeat(37),"é".repeat(37)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.newPassword").exists());
  changePassword(token,"é".repeat(37),"new-password","new-password").andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.currentPassword").exists());
  changePassword(token,"123456","123456","123456").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("PASSWORD_UNCHANGED"));
  assertTrue(passwords.matches("123456",users.findByEmail("mai@example.com").orElseThrow().passwordHash));
 }
 @Test void changePasswordRejectsInactiveAccountAndInvalidSubject() throws Exception {
  register("mai@example.com","0901234567");String token=login("mai@example.com","123456");
  var user=users.findByEmail("mai@example.com").orElseThrow();user.status=com.datn.fashion.entity.User.Status.SUSPENDED;users.saveAndFlush(user);
  changePassword(token,"123456","new-password","new-password").andExpect(status().isUnauthorized());
  var claims=JwtClaimsSet.builder().issuer("fashion-api").subject("invalid").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
  String invalidSubject=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
  changePassword(invalidSubject,"123456","new-password","new-password").andExpect(status().isUnauthorized());
 }
 @Test void passwordChangeIsRateLimitedPerAccount() throws Exception {
  register("mai@example.com","0901234567");register("other@example.com","0911234567");
  String token=login("mai@example.com","123456");
  for(int i=0;i<5;i++) changePassword(token,"wrong-password","new-password","new-password").andExpect(status().isBadRequest());
  changePassword(token,"123456","new-password","new-password").andExpect(status().isTooManyRequests())
   .andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_RATE_LIMITED"));
  changePassword(login("other@example.com","123456"),"123456","new-password","new-password").andExpect(status().isNoContent());
 }
 @Test void registerHashesPasswordAndNormalizesIdentifiers() throws Exception {
  register("MAI@example.com","+84901234567");
  var u=users.findByEmail("mai@example.com").orElseThrow();
  assertEquals("0901234567",u.phoneNumber);assertNotEquals("123456",u.passwordHash);assertTrue(passwords.matches("123456",u.passwordHash));assertEquals(com.datn.fashion.entity.User.Status.ACTIVE,u.status);assertEquals("Nguyễn Mai",profiles.findById(u.id).orElseThrow().fullName);assertEquals(1,profiles.count());
 }
 @Test void duplicateEmail() throws Exception {
  register("mai@example.com","0901234567");
  mvc.perform(post("/api/auth/register").contentType("application/json").content(request(registration("MAI@example.com","0911234567"))))
   .andExpect(status().isConflict()).andExpect(jsonPath("$.fields.email").exists());
 }
 @Test void duplicatePhoneEvenWithDifferentPrefix() throws Exception {
  register("mai@example.com","0901234567");
  mvc.perform(post("/api/auth/register").contentType("application/json").content(request(registration("other@example.com","+84901234567"))))
   .andExpect(status().isConflict()).andExpect(jsonPath("$.fields.phoneNumber").exists());
 }
 @Test void mismatchedConfirmation() throws Exception {
  var body=new java.util.HashMap<>(registration("mai@example.com","0901234567"));body.put("confirmPassword","654321");
  mvc.perform(post("/api/auth/register").contentType("application/json").content(request(body)))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.confirmPassword").exists());
  assertEquals(0,users.count());
 }
 @Test void invalidEmailPhoneAndPassword() throws Exception {
  var body=new java.util.HashMap<>(registration("wrong","123"));body.put("password","123");body.put("confirmPassword","123");
  mvc.perform(post("/api/auth/register").contentType("application/json").content(request(body)))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.email").exists()).andExpect(jsonPath("$.fields.phoneNumber").exists()).andExpect(jsonPath("$.fields.password").exists());
 }
 @Test void loginByEmail() throws Exception { register("mai@example.com","0901234567");assertFalse(login("MAI@example.com","123456").isBlank()); }
 @Test void loginByPhone() throws Exception { register("mai@example.com","0901234567");assertFalse(login("+84901234567","123456").isBlank()); }
 @Test void wrongPassword() throws Exception {
  register("mai@example.com","0901234567");
  mvc.perform(post("/api/auth/login").contentType("application/json").content(request(Map.of("identifier","mai@example.com","password","654321"))))
   .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
 }
 @Test void profileRequiresToken() throws Exception { mvc.perform(get("/api/profile")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED")); }
 @Test void invalidAndExpiredJwt() throws Exception {
  mvc.perform(get("/api/profile").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
  var claims=JwtClaimsSet.builder().issuer("fashion-api").subject("1").issuedAt(Instant.now().minusSeconds(500)).expiresAt(Instant.now().minusSeconds(120)).build();
  String expired=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
  mvc.perform(get("/api/profile").header("Authorization","Bearer "+expired)).andExpect(status().isUnauthorized());
 }
 @Test void profileUsesJwtSubjectAndUpdatesOnlyAllowedFields() throws Exception {
  register("mai@example.com","0901234567");register("other@example.com","0911234567");
  String token=login("mai@example.com","123456");
  mvc.perform(get("/api/profile").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("mai@example.com"));
  mvc.perform(put("/api/profile").header("Authorization","Bearer "+token).contentType("application/json").content(request(Map.of("fullName","Mai mới","phoneNumber","0921234567"))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Mai mới"));
  mvc.perform(put("/api/profile").header("Authorization","Bearer "+token).contentType("application/json").content(request(Map.of("fullName","Mai","phoneNumber","0911234567"))))
   .andExpect(status().isConflict()).andExpect(jsonPath("$.fields.phoneNumber").exists());
  mvc.perform(put("/api/profile").header("Authorization","Bearer "+token).contentType("application/json").content(request(Map.of("fullName","Mai","phoneNumber","0921234567","id",2))))
   .andExpect(status().isBadRequest());
  assertEquals("Nguyễn Mai",profiles.findById(users.findByEmail("other@example.com").orElseThrow().id).orElseThrow().fullName);
  var updated=users.findByEmail("mai@example.com").orElseThrow();assertEquals("0921234567",updated.phoneNumber);assertEquals("Mai mới",profiles.findById(updated.id).orElseThrow().fullName);
 }
 private byte[] png() throws Exception { var out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB),"png",out);return out.toByteArray(); }
 @Test void uploadAvatarValidatesImageAndPersistsSecureUrl() throws Exception {
  register("mai@example.com","0901234567");String token=login("mai@example.com","123456");
  when(avatars.upload(any(byte[].class),anyString(),anyLong())).thenReturn(new AvatarStorage.UploadedAvatar("https://res.cloudinary.com/test/avatar.png","fashion/avatar/test"));
  mvc.perform(multipart("/api/profile/avatar").file(new MockMultipartFile("file","avatar.png","image/png",png())).header("Authorization","Bearer "+token))
   .andExpect(status().isOk()).andExpect(jsonPath("$.avatarUrl").value("https://res.cloudinary.com/test/avatar.png"));
  var user=users.findByEmail("mai@example.com").orElseThrow();assertEquals("https://res.cloudinary.com/test/avatar.png",profiles.findById(user.id).orElseThrow().avatarUrl);verify(avatars,never()).delete(anyString());
  mvc.perform(multipart("/api/profile/avatar").file(new MockMultipartFile("file","fake.png","image/png","not an image".getBytes())).header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());
  mvc.perform(multipart("/api/profile/avatar").file(new MockMultipartFile("file","empty.png","image/png",new byte[0])).header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());
  mvc.perform(multipart("/api/profile/avatar").file(new MockMultipartFile("file","large.png","image/png",new byte[5*1024*1024+1])).header("Authorization","Bearer "+token)).andExpect(status().isPayloadTooLarge());
  mvc.perform(multipart("/api/profile/avatar").file(new MockMultipartFile("file","avatar.png","image/png",png()))).andExpect(status().isUnauthorized());
 }
 @Test void uploadFailureHasFriendlyErrorAndDoesNotChangeProfile() throws Exception {
  register("mai@example.com","0901234567");String token=login("mai@example.com","123456");
  when(avatars.upload(any(byte[].class),anyString(),anyLong())).thenThrow(new com.datn.fashion.exception.ApiException(org.springframework.http.HttpStatus.BAD_GATEWAY,"AVATAR_UPLOAD_FAILED","Không thể tải avatar lên."));
  mvc.perform(multipart("/api/profile/avatar").file(new MockMultipartFile("file","avatar.png","image/png",png())).header("Authorization","Bearer "+token))
   .andExpect(status().isBadGateway()).andExpect(jsonPath("$.code").value("AVATAR_UPLOAD_FAILED"));
  assertNull(profiles.findById(users.findByEmail("mai@example.com").orElseThrow().id).orElseThrow().avatarUrl);
 }

 @Test void profileCreationFailureRollsBackAccount() throws Exception {
  doThrow(new org.springframework.dao.DataIntegrityViolationException("test profile failure"))
   .when(profiles).saveAndFlush(any(com.datn.fashion.entity.UserProfile.class));
  mvc.perform(post("/api/auth/register").contentType("application/json").content(request(registration("mai@example.com","0901234567"))))
   .andExpect(status().isConflict());
  assertEquals(0,users.count());
  assertEquals(0,profiles.count());
 }
 @Test void inactiveAndSuspendedAccountsCannotLogin() throws Exception {
  register("mai@example.com","0901234567");
  for(var state : new com.datn.fashion.entity.User.Status[]{com.datn.fashion.entity.User.Status.INACTIVE,com.datn.fashion.entity.User.Status.SUSPENDED}) {
   var u=users.findByEmail("mai@example.com").orElseThrow();u.status=state;users.saveAndFlush(u);
   mvc.perform(post("/api/auth/login").contentType("application/json").content(request(Map.of("identifier","mai@example.com","password","123456"))))
    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
   assertNull(users.findById(u.id).orElseThrow().lastLoginAt);
  }
 }
 @Test void softDeletedAccountCannotLogin() throws Exception {
  register("mai@example.com","0901234567");
  var u=users.findByEmail("mai@example.com").orElseThrow();u.deletedAt=Instant.now();users.saveAndFlush(u);
  mvc.perform(post("/api/auth/login").contentType("application/json").content(request(Map.of("identifier","0901234567","password","123456"))))
   .andExpect(status().isUnauthorized());
  assertNull(users.findById(u.id).orElseThrow().lastLoginAt);
 }
 @Test void profileAggregatesAccountAndPersonalFields() throws Exception {
  register("mai@example.com","0901234567");
  var u=users.findByEmail("mai@example.com").orElseThrow();
  var p=profiles.findById(u.id).orElseThrow();p.fullName="Tên từ profile";p.avatarUrl="https://res.cloudinary.com/test/existing.png";profiles.saveAndFlush(p);
  String token=login("mai@example.com","123456");
  mvc.perform(get("/api/profile").header("Authorization","Bearer "+token))
   .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(u.id.intValue()))
   .andExpect(jsonPath("$.email").value(u.email)).andExpect(jsonPath("$.phoneNumber").value(u.phoneNumber))
   .andExpect(jsonPath("$.fullName").value(p.fullName)).andExpect(jsonPath("$.avatarUrl").value(p.avatarUrl));
  assertNotNull(users.findById(u.id).orElseThrow().lastLoginAt);
 }
 @Test void legacyAccountWithNullPhoneKeepsResponseCompatible() throws Exception {
  register("mai@example.com","0901234567");
  var u=users.findByEmail("mai@example.com").orElseThrow();u.phoneNumber=null;users.saveAndFlush(u);
  String token=login("mai@example.com","123456");
  mvc.perform(get("/api/profile").header("Authorization","Bearer "+token))
   .andExpect(status().isOk()).andExpect(jsonPath("$.phoneNumber").value(""));
  assertNull(users.findById(u.id).orElseThrow().phoneNumber);
 }
 @Test void namesFollow150CharacterSchemaLimit() throws Exception {
  var body=new java.util.HashMap<>(registration("mai@example.com","0901234567"));body.put("fullName","N".repeat(150));
  mvc.perform(post("/api/auth/register").contentType("application/json").content(request(body))).andExpect(status().isCreated());
  String token=login("mai@example.com","123456");
  mvc.perform(put("/api/profile").header("Authorization","Bearer "+token).contentType("application/json")
   .content(request(Map.of("fullName","N".repeat(151),"phoneNumber","0901234567"))))
   .andExpect(status().isBadRequest());
  assertEquals(150,profiles.findById(users.findByEmail("mai@example.com").orElseThrow().id).orElseThrow().fullName.length());
 }
 @Test void avatarUrlTooLongRollsBackAndCleansNewAsset() throws Exception {
  register("mai@example.com","0901234567");String token=login("mai@example.com","123456");
  when(avatars.upload(any(byte[].class),anyString(),anyLong())).thenReturn(new AvatarStorage.UploadedAvatar("https://res.cloudinary.com/"+"a".repeat(1000),"new-too-long"));
  mvc.perform(multipart("/api/profile/avatar").file(new MockMultipartFile("file","avatar.png","image/png",png())).header("Authorization","Bearer "+token))
   .andExpect(status().isBadRequest());
  assertNull(profiles.findById(users.findByEmail("mai@example.com").orElseThrow().id).orElseThrow().avatarUrl);
  verify(avatars).delete("new-too-long");
 }
 @Test void missingProfileIsReportedWithoutCreatingData() throws Exception {
  register("mai@example.com","0901234567");String token=login("mai@example.com","123456");
  profiles.deleteAll();
  mvc.perform(get("/api/profile").header("Authorization","Bearer "+token))
   .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("PROFILE_NOT_FOUND"));
  assertEquals(1,users.count());assertEquals(0,profiles.count());
 }

 @Test void personalDetailsPersistAcrossLoginAndExplicitNullClearsThem() throws Exception {
  register("mai@example.com","0901234567"); String token=login("mai@example.com","123456");
  mvc.perform(put("/api/profile").header("Authorization","Bearer "+token).contentType("application/json")
   .content(request(Map.of("fullName","Mai cập nhật","phoneNumber","+84921234567","dateOfBirth","2000-02-29","gender","PREFER_NOT_TO_SAY"))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Mai cập nhật"))
   .andExpect(jsonPath("$.phoneNumber").value("0921234567"))
   .andExpect(jsonPath("$.dateOfBirth").value("2000-02-29"))
   .andExpect(jsonPath("$.gender").value("PREFER_NOT_TO_SAY"));
  var account=users.findByEmail("mai@example.com").orElseThrow();
  var saved=profiles.findById(account.id).orElseThrow();
  assertEquals(java.time.LocalDate.of(2000,2,29),saved.dateOfBirth);
  assertEquals(com.datn.fashion.entity.UserProfile.Gender.PREFER_NOT_TO_SAY,saved.gender);
  token=login("0921234567","123456");
  mvc.perform(get("/api/profile").header("Authorization","Bearer "+token))
   .andExpect(status().isOk()).andExpect(jsonPath("$.dateOfBirth").value("2000-02-29"))
   .andExpect(jsonPath("$.gender").value("PREFER_NOT_TO_SAY"))
   .andExpect(jsonPath("$.email").value("mai@example.com"));
  mvc.perform(put("/api/profile").header("Authorization","Bearer "+token).contentType("application/json")
   .content("{\"fullName\":\"Mai cập nhật\",\"phoneNumber\":\"0921234567\",\"dateOfBirth\":null,\"gender\":null}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.dateOfBirth").isEmpty()).andExpect(jsonPath("$.gender").isEmpty());
  saved=profiles.findById(account.id).orElseThrow(); assertNull(saved.dateOfBirth);assertNull(saved.gender);
 }

 @Test void invalidPersonalDetailsAndEmailChangesDoNotWriteAnything() throws Exception {
  register("mai@example.com","0901234567"); String token=login("mai@example.com","123456");
  for(var entry : Map.of("dateOfBirth",java.time.LocalDate.now().plusDays(1).toString(),"gender","UNKNOWN","phoneNumber","123").entrySet()) {
   var body=new java.util.HashMap<String,Object>(Map.of("fullName","Không được lưu","phoneNumber","0921234567"));
   body.put(entry.getKey(),entry.getValue());
   mvc.perform(put("/api/profile").header("Authorization","Bearer "+token).contentType("application/json").content(request(body)))
    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields."+entry.getKey()).exists());
  }
  for(var entry : Map.of("dateOfBirth","2001-02-30","email","changed@example.com").entrySet()) {
   var body=new java.util.HashMap<String,Object>(Map.of("fullName","Không được lưu","phoneNumber","0921234567"));body.put(entry.getKey(),entry.getValue());
   mvc.perform(put("/api/profile").header("Authorization","Bearer "+token).contentType("application/json").content(request(body)))
    .andExpect(status().isBadRequest());
  }
  mvc.perform(put("/api/profile").header("Authorization","Bearer "+token).contentType("application/json")
   .content(request(Map.of("fullName","Không được lưu","phoneNumber","0921234567","dateOfBirth","0999-12-31"))))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.dateOfBirth").exists());
  var account=users.findByEmail("mai@example.com").orElseThrow();
  assertEquals("0901234567",account.phoneNumber);
  var saved=profiles.findById(account.id).orElseThrow();assertEquals("Nguyễn Mai",saved.fullName);
  assertNull(saved.dateOfBirth);assertNull(saved.gender);
 }

 @Test void allDatabaseGenderValuesAreAccepted() throws Exception {
  register("mai@example.com","0901234567");String token=login("mai@example.com","123456");
  for(var gender:com.datn.fashion.entity.UserProfile.Gender.values()) {
   mvc.perform(put("/api/profile").header("Authorization","Bearer "+token).contentType("application/json")
    .content(request(Map.of("fullName","Nguyễn Mai","phoneNumber","0901234567","gender",gender.name(),"dateOfBirth",java.time.LocalDate.now().toString()))))
    .andExpect(status().isOk()).andExpect(jsonPath("$.gender").value(gender.name()));
  }
 }

 @Test void profileReturnsRealBirthAndGenderWithoutChangingExistingUpdateContract() throws Exception {
  register("mai@example.com","0901234567");String token=login("mai@example.com","123456");
  var profile=profiles.findById(users.findByEmail("mai@example.com").orElseThrow().id).orElseThrow();
  profile.dateOfBirth=java.time.LocalDate.of(2002,5,16);
  profile.gender=com.datn.fashion.entity.UserProfile.Gender.FEMALE;
  profiles.saveAndFlush(profile);
  mvc.perform(get("/api/profile").header("Authorization","Bearer "+token))
   .andExpect(status().isOk()).andExpect(jsonPath("$.dateOfBirth").value("2002-05-16"))
   .andExpect(jsonPath("$.gender").value("FEMALE"));
  mvc.perform(put("/api/profile").header("Authorization","Bearer "+token).contentType("application/json")
   .content(request(Map.of("fullName","Mai mới","phoneNumber","0901234567"))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.dateOfBirth").value("2002-05-16"))
   .andExpect(jsonPath("$.gender").value("FEMALE"));
 }

 @Test void loginIgnoresInvalidBearerOnPublicEndpoint() throws Exception {
  register("mai@example.com","0901234567");
  mvc.perform(post("/api/auth/login").header("Authorization","Bearer invalid-old-token")
   .contentType("application/json").content(request(Map.of("identifier","mai@example.com","password","123456"))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
 }
 @Test void loginIgnoresExpiredBearerOnPublicEndpoint() throws Exception {
  register("mai@example.com","0901234567");
  var claims=JwtClaimsSet.builder().issuer("fashion-api").subject("999")
   .issuedAt(Instant.now().minusSeconds(500)).expiresAt(Instant.now().minusSeconds(120)).build();
  String expired=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
  mvc.perform(post("/api/auth/login").header("Authorization","Bearer "+expired)
   .contentType("application/json").content(request(Map.of("identifier","0901234567","password","123456"))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.user.email").value("mai@example.com"));
 }
 @Test void registerIgnoresInvalidBearerOnPublicEndpoint() throws Exception {
  mvc.perform(post("/api/auth/register").header("Authorization","Bearer invalid-old-token")
   .contentType("application/json").content(request(registration("mai@example.com","0901234567"))))
   .andExpect(status().isCreated());
 }
 @Test void loginWithInvalidBearerStillVerifiesPassword() throws Exception {
  register("mai@example.com","0901234567");
  mvc.perform(post("/api/auth/login").header("Authorization","Bearer invalid-old-token")
   .contentType("application/json").content(request(Map.of("identifier","mai@example.com","password","wrong-password"))))
   .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
 }
 @Test void loginJsonContractIsValidatedWithoutJwt() throws Exception {
  mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  mvc.perform(post("/api/auth/login").header("Authorization","Bearer invalid-old-token")
   .contentType("application/json").content(request(Map.of("email","mai@example.com","password","123456"))))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
 }
 @Test void protectedProfileStillRejectsInvalidJwtWithDistinctError() throws Exception {
  mvc.perform(get("/api/profile").header("Authorization","Bearer invalid-old-token"))
   .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
   .andExpect(jsonPath("$.message").value("Phiên đăng nhập hết hạn hoặc không hợp lệ."));
 }
 @Test void jwtBypassAppliesOnlyToExactPostAuthEndpoints() throws Exception {
  mvc.perform(get("/api/auth/login").header("Authorization","Bearer invalid-old-token"))
   .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  mvc.perform(post("/api/auth/login/extra").header("Authorization","Bearer invalid-old-token"))
   .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
 }
 @Test void publicLoginJwtBypassWorksWithContextPath() throws Exception {
  register("mai@example.com","0901234567");
  mvc.perform(post("/nova/api/auth/login").contextPath("/nova").header("Authorization","Bearer invalid-old-token")
   .contentType("application/json").content(request(Map.of("identifier","mai@example.com","password","123456"))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
 }
}
