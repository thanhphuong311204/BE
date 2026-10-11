package com.datn.fashion.repository;
import com.datn.fashion.dto.explore.StyleGuideResponse;
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
