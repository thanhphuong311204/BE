package com.datn.fashion;
import com.datn.fashion.entity.User;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.service.AvatarStorage;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class ExploreCatalogIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired JdbcTemplate jdbc;
 @Autowired UserRepository users;
 @Autowired ObjectMapper json; @Autowired jakarta.persistence.EntityManager entityManager;
 @MockitoSpyBean com.datn.fashion.repository.UserProfileRepository profiles;
 @MockitoBean AvatarStorage avatars;
 Long userId;
 private void seed(String filename) throws Exception {
  String sql=Files.readString(Path.of("database/seeds",filename),StandardCharsets.UTF_8)
   .replace("USE fashion_ai;","").replace("SET NAMES utf8mb4;","")
   .replace("START TRANSACTION;","").replace("COMMIT;","");
  var populator=new ResourceDatabasePopulator(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)));
  populator.execute(Objects.requireNonNull(jdbc.getDataSource()));
 }
 @BeforeEach void setup() throws Exception {
  profiles.deleteAll(); users.deleteAll();
  for(String table:List.of("style_guides","style_translations","color_translations","occasion_translations","styles","style_groups","languages"))
   jdbc.update("DELETE FROM "+table);
  var user=new User(); user.email="explore-test@example.com"; user.passwordHash="fixture";
  userId=users.saveAndFlush(user).id;
  jdbc.update("INSERT INTO languages(code,name,native_name) VALUES('vi','Vietnamese','Vietnamese'),('en','English','English')");
  jdbc.update("INSERT INTO styles(id,code) VALUES(701,'OLD_MONEY'),(702,'FEMININE')");
  seed("003_explore_catalog.sql");
  seed("004_old_money_guide.sql");
 }
 @Test void exactCatalogAndSeedRerunsPreserveIdsAndLegacy() throws Exception {
  seed("003_explore_catalog.sql"); seed("004_old_money_guide.sql");
  assertEquals(40,jdbc.queryForObject("SELECT COUNT(*) FROM styles WHERE is_explore_enabled=TRUE",Integer.class));
  assertEquals(701L,jdbc.queryForObject("SELECT id FROM styles WHERE code='OLD_MONEY'",Long.class));
  assertFalse(jdbc.queryForObject("SELECT is_explore_enabled FROM styles WHERE code='FEMININE'",Boolean.class));
  mvc.perform(get("/api/explore/styles").with(jwt().jwt(j->j.subject(userId.toString()))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.styles.length()").value(40))
   .andExpect(jsonPath("$.groups.length()").value(5));
 }
 @Test void detailProvidesTenStructuredSectionsAndSixRealFormulasBySlugAndId() throws Exception {
  var result=mvc.perform(get("/api/explore/styles/old-money").with(jwt().jwt(j->j.subject(userId.toString()))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.style.id").value(701))
   .andExpect(jsonPath("$.guide.outfits.length()").value(6))
   .andExpect(jsonPath("$.guide.gettingStarted.essentials.length()").value(5))
   .andExpect(jsonPath("$.style.imageUrl").isEmpty()).andReturn();
  var guide=json.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("guide");
  for(String field:List.of("overview","recognition","palette","garments","accessories","outfits","occasions","suitability","mistakes","gettingStarted"))
   assertFalse(guide.path(field).isEmpty(),field);
  guide.get("palette").forEach(c->assertTrue(c.get("hex").asText().matches("#[A-Fa-f0-9]{6}")));
  guide.get("outfits").forEach(o-> { assertTrue(o.get("items").size()>=3); assertTrue(o.get("explanation").asText().length()>80); assertTrue(o.get("imageUrl").isNull()); });
  mvc.perform(get("/api/explore/styles/by-id/701").with(jwt().jwt(j->j.subject(userId.toString()))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.style.slug").value("old-money")).andExpect(jsonPath("$.guide.outfits.length()").value(6));
 }
 @Test void listIsLightweightAndIncompleteGuidesAreExplicitlyNull() throws Exception {
  var result=mvc.perform(get("/api/explore/styles").with(jwt().jwt(j->j.subject(userId.toString()))))
   .andExpect(status().isOk()).andReturn();
  for(var style:json.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("styles"))
   assertFalse(style.has("guide"));
  mvc.perform(get("/api/explore/styles/minimalist").with(jwt().jwt(j->j.subject(userId.toString()))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.guide").isEmpty());
 }
 @Test void inactiveAndMissingStylesReturn404() throws Exception {
  jdbc.update("UPDATE styles SET is_active=FALSE WHERE code='OLD_MONEY'");
  for(String slug:List.of("old-money","unknown","' OR 1=1 --"))
   mvc.perform(get("/api/explore/styles/{slug}",slug).with(jwt().jwt(j->j.subject(userId.toString())))).andExpect(status().isNotFound());
 }
 @Test void authenticationAndAccountStatusAreRequired() throws Exception {
  mvc.perform(get("/api/explore/styles")).andExpect(status().isUnauthorized());
  jdbc.update("UPDATE users SET status='SUSPENDED' WHERE id=?",userId); entityManager.clear();
  mvc.perform(get("/api/explore/styles").with(jwt().jwt(j->j.subject(userId.toString())))).andExpect(status().isUnauthorized());
 }
}
