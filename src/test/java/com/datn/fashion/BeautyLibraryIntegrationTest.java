package com.datn.fashion;
import com.datn.fashion.entity.*;
import com.datn.fashion.repository.*;
import com.datn.fashion.security.JwtService;
import com.datn.fashion.service.FaceInferenceClient;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:beautylibrary;MODE=MySQL;DB_CLOSE_DELAY=-1","debug=false","logging.level.root=WARN","logging.level.org.springframework=WARN","logging.level.org.hibernate=WARN"})
@AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class BeautyLibraryIntegrationTest {
 @Autowired MockMvc mvc;@Autowired UserRepository users;@Autowired UserProfileRepository profiles;@Autowired JwtService jwt;@Autowired ObjectMapper json;
 @MockitoBean FaceInferenceClient inference;
 String token;User owner;
 @BeforeEach void setup(){owner=new User();owner.email="library-fixture@example.com";owner.passwordHash="test-only";owner=users.saveAndFlush(owner);var p=new UserProfile();p.user=owner;p.fullName="Library fixture";p.confirmedFaceShape=FaceShapeHistory.Shape.OVAL;profiles.saveAndFlush(p);token=jwt.create(owner.id);}
 JsonNode query(String q)throws Exception {return json.readTree(mvc.perform(get("/api/beauty/recommendations?library=true&scope=ALL&"+q).header("Authorization","Bearer "+token)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));}
 @Test void fortyDistinctItemsHaveRichGuidesAndValidRelatedContent()throws Exception {
  var ids=new HashSet<String>();var slugs=new HashSet<String>();int real=0;
  for(var type:List.of("MAKEUP","HAIRSTYLE")){
   var data=query("type="+type+"&size=50");assertEquals(type.equals("MAKEUP")?21:20,data.path("total").asInt());
   for(var i:data.path("items")){assertTrue(ids.add(i.path("id").asText()));assertTrue(slugs.add(i.path("library").path("slug").asText()));assertTrue(i.path("steps").size()>=5);assertTrue(i.path("library").path("explanation").asText().length()>80);assertFalse(i.path("library").path("palette").isEmpty());assertFalse(i.path("related").isEmpty());for(var r:i.path("related"))assertNotEquals(type,r.path("type").asText());if(!i.path("thumbnail").isNull())real++;}
  }
  assertEquals(41,ids.size());assertEquals(17,real);verifyNoInteractions(inference);
 }
 @Test void searchFiltersAndPaginationWorkTogether()throws Exception {
  var first=query("type=MAKEUP&size=3&page=0");var second=query("type=MAKEUP&size=3&page=1");assertTrue(first.path("hasNext").asBoolean());assertEquals(3,second.path("items").size());assertNotEquals(first.path("items").get(0).path("id"),second.path("items").get(0).path("id"));
  assertTrue(query("q=butterfly").path("total").asInt()>0);
  var shortHair=query("type=HAIRSTYLE&hairLength=SHORT&season=SUMMER&difficulty=EASY");assertTrue(shortHair.path("total").asInt()>0);
  for(var i:shortHair.path("items"))assertTrue(List.of("SHORT","ANY").contains(i.path("library").path("hairLength").asText()));
  assertEquals(0,query("q=not-a-style-123456").path("total").asInt());
 }
 @Test void savedShapeDrivesOrderAndDetailWithoutRescan()throws Exception {
  var data=query("type=MAKEUP&size=20");assertEquals("OVAL",data.path("confirmedFaceShape").asText());assertTrue(data.path("items").get(0).path("matchesConfirmedShape").asBoolean());
  mvc.perform(get("/api/beauty/recommendations/hair-butterfly?library=true").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.confirmedFaceShape").value("OVAL")).andExpect(jsonPath("$.item.library.slug").value("hair-butterfly"));
  assertEquals(FaceShapeHistory.Shape.OVAL,profiles.findById(owner.id).orElseThrow().confirmedFaceShape);verifyNoInteractions(inference);
 }
 @Test void noSavedProfileCanBrowseAndParametersAreValidated()throws Exception {
  var p=profiles.findById(owner.id).orElseThrow();p.confirmedFaceShape=null;profiles.saveAndFlush(p);assertTrue(query("type=MAKEUP").path("confirmedFaceShape").isNull());
  for(String q:List.of("page=-1","size=0","size=51","season=MONSOON","occasion=UNKNOWN","difficulty=EXPERT","hairLength=HUGE","type=OTHER","faceShape=DIAMOND"))mvc.perform(get("/api/beauty/recommendations?library=true&"+q).header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());
  mvc.perform(get("/api/beauty/recommendations/unknown?library=true").header("Authorization","Bearer "+token)).andExpect(status().isNotFound());
  mvc.perform(get("/api/beauty/recommendations?library=true")).andExpect(status().isUnauthorized());
 }
 @Test void threeLengthCategoriesAndSecondaryHairFilters()throws Exception {
  for(var length:List.of("SHORT","MEDIUM","LONG")){
   var data=query("type=HAIRSTYLE&size=50&hairLength="+length);assertTrue(data.path("total").asInt()>0);
   for(var i:data.path("items")){boolean found=false;for(var v:i.path("library").path("hairLengths"))if(v.asText().equals(length))found=true;assertTrue(found);}
  }
  for(var type:List.of("STRAIGHT","WAVY","CURLY","LAYERED","BANGS","BRAIDED","UPDO","HALF_UP"))assertTrue(query("type=HAIRSTYLE&hairType="+type).path("total").asInt()>0);
  assertTrue(query("type=HAIRSTYLE&hairLength=LONG&hairType=LAYERED&hairColor=CARAMEL").path("total").asInt()>0);
  for(var bad:List.of("hairType=INVALID","hairColor=PINK"))mvc.perform(get("/api/beauty/recommendations?library=true&"+bad).header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());
 }
 @Test void everyMakeupHasTenSectionsFivePaletteRolesAndExplicitVariants()throws Exception {
  for(var i:query("type=MAKEUP&size=50").path("items")){
   var g=i.path("library");assertEquals(10,g.path("makeupSteps").size());assertEquals(5,g.path("makeupPalette").size());
   var roles=new HashSet<String>();for(var group:g.path("makeupPalette")){roles.add(group.path("role").asText());assertTrue(group.path("colors").size()>0);for(var color:group.path("colors"))assertTrue(color.path("hex").asText().matches("#[0-9a-fA-F]{6}"));}
   assertEquals(Set.of("LIP","BLUSH","EYESHADOW","EYEBROW","EYELINER"),roles);
  }
  verifyNoInteractions(inference);
 }
 @Test void accentInsensitiveSearchAndLegacyAnyLengthRemainCompatible()throws Exception {
  assertTrue(query("q=toc&type=HAIRSTYLE").path("total").asInt()>0);
  assertEquals(20,query("type=HAIRSTYLE&hairLength=ANY&size=50").path("total").asInt());
 }

 @Test void makeupTaxonomyStrawberryAndLatteAreCompatible()throws Exception {
  assertEquals(21,query("type=MAKEUP&size=50").path("total").asInt());
  assertEquals(14,query("type=MAKEUP&makeupCategory=LOOK&size=50").path("total").asInt());
  assertEquals(7,query("type=MAKEUP&makeupCategory=TECHNIQUE&size=50").path("total").asInt());
  assertEquals("makeup-soft-brown",query("type=MAKEUP&q=Latte").path("items").get(0).path("id").asText());
  assertEquals("makeup-strawberry",query("type=MAKEUP&q=Strawberry").path("items").get(0).path("id").asText());
  mvc.perform(get("/api/beauty/recommendations/makeup-strawberry?faceShape=OVAL").header("Authorization","Bearer "+token)).andExpect(status().isOk());
  mvc.perform(get("/api/beauty/recommendations?library=true&type=MAKEUP&makeupCategory=INVALID").header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());
  mvc.perform(get("/api/beauty/recommendations?library=true&type=HAIRSTYLE&makeupCategory=LOOK").header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());
 }
}
