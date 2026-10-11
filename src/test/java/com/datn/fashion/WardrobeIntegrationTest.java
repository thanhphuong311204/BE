package com.datn.fashion;
import com.datn.fashion.entity.User;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.service.*;
import com.datn.fashion.dto.media.ImageUploadResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.*;import java.nio.charset.StandardCharsets;import java.util.*;
import java.awt.image.BufferedImage;import javax.imageio.ImageIO;import java.io.ByteArrayOutputStream;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:wardrobe_test;MODE=MySQL;DB_CLOSE_DELAY=-1") @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class WardrobeIntegrationTest {
 @Autowired MockMvc mvc;@Autowired JdbcTemplate jdbc;@Autowired UserRepository users;@Autowired ObjectMapper json;
 @MockitoBean CloudinaryImageStorage cloud;
 Long owner,other,main,sub,color,style;
 void seed(String file) throws Exception {
  var sql=Files.readString(Path.of("database/seeds",file),StandardCharsets.UTF_8)
   .replace("USE fashion_ai;","").replace("SET NAMES utf8mb4;","").replace("START TRANSACTION;","").replace("COMMIT;","");
  new ResourceDatabasePopulator(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8))).execute(Objects.requireNonNull(jdbc.getDataSource()));
 }
 @BeforeEach void setup() throws Exception { reset(cloud);
  for(String table:List.of("wardrobe_items","clothing_category_translations","clothing_categories","user_preferred_styles","user_preferred_colors","user_preferred_occasions","user_style_preferences","style_guides","style_translations","color_translations","occasion_translations","styles","style_groups","colors","occasions","languages","user_profiles","users"))jdbc.update("DELETE FROM "+table);
  var user=new User();user.email="wardrobe-owner@example.com";user.passwordHash="fixture";owner=users.saveAndFlush(user).id;
  var user2=new User();user2.email="wardrobe-other@example.com";user2.passwordHash="fixture";other=users.saveAndFlush(user2).id;
  jdbc.update("INSERT INTO languages(code,name,native_name) VALUES('vi','Vietnamese','Vietnamese'),('en','English','English')");
  seed("003_explore_catalog.sql");seed("005_wardrobe_categories.sql");
  jdbc.update("INSERT INTO colors(code,hex_code) VALUES('IVORY','#FFFFF0')");
  main=jdbc.queryForObject("SELECT id FROM clothing_categories WHERE code='TOPS'",Long.class);
  sub=jdbc.queryForObject("SELECT id FROM clothing_categories WHERE code='TOPS_SHIRT'",Long.class);
  color=jdbc.queryForObject("SELECT id FROM colors WHERE code='IVORY'",Long.class);
  style=jdbc.queryForObject("SELECT id FROM styles WHERE code='OLD_MONEY'",Long.class);
  when(cloud.upload(any(),anyString(),eq(ImageUploadTarget.WARDROBE),eq(owner)))
   .thenReturn(new ImageUploadResponse("https://res.cloudinary.com/test/image/upload/wardrobe.png","nova-atelier/users/wardrobe/"+owner+"/test"));
 }
 Map<String,Object> body(){return new HashMap<>(Map.of("name","Áo sơ mi trắng","mainCategoryId",main,"subCategoryId",sub,"colorId",color,"styleIds",List.of(style),"notes","Cotton"));}
 MockMultipartFile image() throws Exception {var out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(4,4,BufferedImage.TYPE_INT_RGB),"png",out);return new MockMultipartFile("file","shirt.png","image/png",out.toByteArray());}
 Long create() throws Exception {
  var r=mvc.perform(multipart("/api/wardrobe/items").file(image()).param("item",json.writeValueAsString(body())).with(jwt().jwt(j->j.subject(owner.toString()))))
   .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Áo sơ mi trắng")).andExpect(jsonPath("$.styleIds[0]").value(style)).andReturn();
  return json.readTree(r.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();
 }
 @Test void realOptionsAndIdempotentCategorySeed() throws Exception {
  seed("005_wardrobe_categories.sql");
  assertEquals(28,jdbc.queryForObject("SELECT COUNT(*) FROM clothing_categories",Integer.class));
  mvc.perform(get("/api/wardrobe/options").with(jwt().jwt(j->j.subject(owner.toString()))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.categories.length()").value(28))
   .andExpect(jsonPath("$.styles.length()").value(40)).andExpect(jsonPath("$.colors[0].hexCode").value("#FFFFF0"));
 }
 @Test void createStoresOwnerImageAndStyleLinksAndFilters() throws Exception {
  var id=create();assertEquals(owner,jdbc.queryForObject("SELECT user_id FROM wardrobe_items WHERE id=?",Long.class,id));
  verify(cloud).upload(any(),anyString(),eq(ImageUploadTarget.WARDROBE),eq(owner));
  mvc.perform(get("/api/wardrobe/items").param("mainCategoryId",main.toString()).param("subCategoryId",sub.toString()).param("colorId",color.toString()).with(jwt().jwt(j->j.subject(owner.toString()))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1));
  mvc.perform(get("/api/wardrobe/items").param("colorId","99999").with(jwt().jwt(j->j.subject(owner.toString()))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(0));
 }
 @Test void otherAccountCannotReadUpdateOrDelete() throws Exception {
  Long id=create();
  mvc.perform(get("/api/wardrobe/items").with(jwt().jwt(j->j.subject(other.toString())))).andExpect(jsonPath("$.items.length()").value(0));
  mvc.perform(get("/api/wardrobe/items/"+id).with(jwt().jwt(j->j.subject(other.toString())))).andExpect(status().isNotFound());
  mvc.perform(put("/api/wardrobe/items/"+id).contentType("application/json").content(json.writeValueAsString(body())).with(jwt().jwt(j->j.subject(other.toString())))).andExpect(status().isNotFound());
  mvc.perform(delete("/api/wardrobe/items/"+id).with(jwt().jwt(j->j.subject(other.toString())))).andExpect(status().isNotFound());
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM wardrobe_items",Integer.class));
 }
 @Test void metadataCanBeEditedAndRemoved() throws Exception {
  Long id=create();var data=body();data.put("name","Áo sơ mi cotton");data.put("styleIds",List.of());data.remove("subCategoryId");
  mvc.perform(put("/api/wardrobe/items/"+id).contentType("application/json").content(json.writeValueAsString(data)).with(jwt().jwt(j->j.subject(owner.toString()))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Áo sơ mi cotton")).andExpect(jsonPath("$.styleIds.length()").value(0)).andExpect(jsonPath("$.subCategoryId").isEmpty());
  mvc.perform(delete("/api/wardrobe/items/"+id).with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(status().isOk());
  assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM wardrobe_items",Integer.class));
  assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM wardrobe_item_styles",Integer.class));
 }
 @Test void invalidMetadataIsRejectedBeforeUploading() throws Exception {
  for(String field:List.of("name","mainCategoryId","subCategoryId","colorId","styleIds")) {
   var data=body();data.put(field,switch(field){case "name"->" ";case "styleIds"->List.of(style,style);default->999999;});
   mvc.perform(multipart("/api/wardrobe/items").file(image()).param("item",json.writeValueAsString(data)).with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(status().isBadRequest());
  }
  verifyNoInteractions(cloud);
 }
 @Test void invalidFileOrMissingJwtCannotCreate() throws Exception {
  mvc.perform(multipart("/api/wardrobe/items").file(new MockMultipartFile("file","bad.png","image/png",new byte[]{1,2})).param("item",json.writeValueAsString(body())).with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(status().isBadRequest());
  mvc.perform(get("/api/wardrobe/items")).andExpect(status().isUnauthorized());verifyNoInteractions(cloud);
 }
 @Test void cloudFailureLeavesNoRecord() throws Exception {
  when(cloud.upload(any(),anyString(),any(),anyLong())).thenThrow(new com.datn.fashion.exception.ApiException(org.springframework.http.HttpStatus.BAD_GATEWAY,"IMAGE_UPLOAD_FAILED","Upload thất bại"));
  mvc.perform(multipart("/api/wardrobe/items").file(image()).param("item",json.writeValueAsString(body())).with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(status().isBadGateway());
  assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM wardrobe_items",Integer.class));
 }

 @Autowired WardrobeService service;
 @org.springframework.test.context.bean.override.mockito.MockitoSpyBean com.datn.fashion.repository.WardrobeRepository repository;
 @Test @Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
 void cloudAssetDeletedOnlyAfterDatabaseCommit() throws Exception {
  Long id=create();
  mvc.perform(delete("/api/wardrobe/items/"+id).with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(status().isOk());
  verify(cloud).delete("nova-atelier/users/wardrobe/"+owner+"/test");
 }
 @Test @Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
 void databaseFailureRollsBackAndCleansNewCloudAsset() throws Exception {
  doThrow(new org.springframework.dao.DataIntegrityViolationException("simulated insert failure"))
   .when(repository).create(eq(owner),any(),anyString(),anyString());
  try {
   mvc.perform(multipart("/api/wardrobe/items").file(image()).param("item",json.writeValueAsString(body())).with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(status().isInternalServerError());
   assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM wardrobe_items",Integer.class));
   verify(cloud).delete("nova-atelier/users/wardrobe/"+owner+"/test");
  } finally { reset(repository); }
 }
 @Test void realChildOfAnotherRootIsRejected() throws Exception {
  var data=body();data.put("subCategoryId",jdbc.queryForObject("SELECT id FROM clothing_categories WHERE code='BOTTOMS_JEANS'",Long.class));
  mvc.perform(multipart("/api/wardrobe/items").file(image()).param("item",json.writeValueAsString(data)).with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(status().isBadRequest());
  verifyNoInteractions(cloud);
 }
}
