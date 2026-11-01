package com.datn.fashion.repository;
import com.datn.fashion.dto.explore.StyleGuideResponse;
import com.datn.fashion.dto.explore.StyleVisualResponse;
import com.datn.fashion.dto.explore.StyleImageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataRetrievalFailureException;
import java.util.Optional;

@Repository
public class StyleGuideRepository {
 private final JdbcTemplate jdbc;
 private final ObjectMapper mapper;
 public StyleGuideRepository(JdbcTemplate jdbc,ObjectMapper mapper) { this.jdbc=jdbc; this.mapper=mapper; }
 public java.util.List<StyleImageResponse> images(Long styleId) {
  return jdbc.query("SELECT id,outfit_key,image_url,caption FROM style_outfit_images WHERE style_id=? ORDER BY id",
   (r,i) -> new StyleImageResponse(r.getLong("id"),r.getString("outfit_key"),r.getString("image_url"),r.getString("caption")),styleId);
 }
 public StyleVisualResponse visuals(Long styleId) {
  return jdbc.query("SELECT content FROM style_visual_profiles WHERE style_id=?", (r,i) -> {
   try { return mapper.readValue(r.getString("content"),StyleVisualResponse.class); }
   catch(java.io.IOException ex) { throw new DataRetrievalFailureException("Invalid visual style profile",ex); }
  },styleId).stream().findFirst().orElseGet(StyleVisualResponse::empty);
 }
 public Optional<StyleGuideResponse> get(Long styleId) {
  return jdbc.query("""
   SELECT g.content FROM style_guides g JOIN languages l ON l.id=g.language_id
   WHERE g.style_id=? AND l.code='vi'
   """,(r,i) -> {
    try { return mapper.readValue(r.getString("content"),StyleGuideResponse.class); }
    catch(java.io.IOException ex) { throw new DataRetrievalFailureException("Invalid structured style guide",ex); }
   },styleId).stream().findFirst();
 }
}
