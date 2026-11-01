package com.datn.fashion;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.nio.charset.StandardCharsets;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
class OutfitRecommendationIntegrationTest extends WardrobeIntegrationTest {
 Long category(String code) {return jdbc.queryForObject("SELECT id FROM clothing_categories WHERE code=?",Long.class,code);}
 Long add(Long user,String root,String subtype,String name) {
  jdbc.update("INSERT INTO wardrobe_items(user_id,name,main_category_id,sub_category_id,color_id,image_url,image_public_id) VALUES(?,?,?,?,?,?,?)",user,name,category(root),subtype==null?null:category(subtype),color,"https://res.cloudinary.com/test/item.png","test/"+name);
  return jdbc.queryForObject("SELECT id FROM wardrobe_items WHERE user_id=? AND name=?",Long.class,user,name);
 }
 @Test void jwtScopesBothOwnersAndCrudChangesAreImmediatelyReflected() throws Exception {
  Long shirt=add(owner,"TOPS",null,"owner-shirt"), trousers=add(owner,"BOTTOMS",null,"owner-trousers");
  Long dress=add(other,"DRESSES_SKIRTS","DRESSES_SKIRTS_DRESS","other-dress");
  String url="/api/wardrobe/outfit-recommendations";
  mvc.perform(get(url)).andExpect(status().isUnauthorized());
  var a=mvc.perform(get(url).with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(status().isOk()).andExpect(jsonPath("$.method").value("rule_based_v1")).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
  assertEquals(a,mvc.perform(get(url+"?userId="+other).with(jwt().jwt(j->j.subject(owner.toString())))).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
  assertTrue(a.contains("owner-shirt"));assertTrue(a.contains("owner-trousers"));assertFalse(a.contains("other-dress"));
  var b=mvc.perform(get(url).with(jwt().jwt(j->j.subject(other.toString())))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
  assertTrue(b.contains("other-dress"));assertFalse(b.contains("owner-shirt"));
  assertEquals(a,mvc.perform(get(url).with(jwt().jwt(j->j.subject(owner.toString())))).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
  service.delete(owner,trousers);
  mvc.perform(get(url).with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(jsonPath("$.status").value("INSUFFICIENT_DATA")).andExpect(jsonPath("$.outfits").isEmpty());
 }
 @Test void preferencesAffectRealEndpointScore() throws Exception {
  Long shirt=add(owner,"TOPS",null,"preferred-shirt");add(owner,"BOTTOMS",null,"trousers");
  jdbc.update("INSERT INTO wardrobe_item_styles(item_id,style_id) VALUES(?,?)",shirt,style);
  jdbc.update("INSERT INTO user_style_preferences(user_id,status) VALUES(?,'COMPLETED')",owner);
  jdbc.update("INSERT INTO user_preferred_styles(user_id,style_id) VALUES(?,?)",owner,style);
  jdbc.update("INSERT INTO user_preferred_colors(user_id,color_id) VALUES(?,?)",owner,color);
  mvc.perform(get("/api/wardrobe/outfit-recommendations").with(jwt().jwt(j->j.subject(owner.toString()))))
    .andExpect(status().isOk()).andExpect(jsonPath("$.outfits[0].score").value(24));
 }
 @Test void validatesPaginationAndMalformedSubject() throws Exception {
  mvc.perform(get("/api/wardrobe/outfit-recommendations?page=invalid").with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(status().isBadRequest());
  mvc.perform(get("/api/wardrobe/outfit-recommendations?page=-1").with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(status().isBadRequest());
  mvc.perform(get("/api/wardrobe/outfit-recommendations?limit=13").with(jwt().jwt(j->j.subject(owner.toString())))).andExpect(status().isBadRequest());
  mvc.perform(get("/api/wardrobe/outfit-recommendations").with(jwt().jwt(j->j.subject("invalid")))).andExpect(status().isUnauthorized());
 }
}
