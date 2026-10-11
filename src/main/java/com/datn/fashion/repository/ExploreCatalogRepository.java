package com.datn.fashion.repository;
import com.datn.fashion.dto.explore.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ExploreCatalogRepository {
 private final JdbcTemplate jdbc;
 public ExploreCatalogRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
 private static final String SELECT = """
 SELECT s.id, s.code, s.slug, s.image_url,
 COALESCE(te.name, s.code) AS name_en, COALESCE(tv.name, te.name, s.code) AS name_vi,
 tv.description AS description_vi, te.description AS description_en,
 g.id AS group_id, g.code AS group_code, g.name_en AS group_en, g.name_vi AS group_vi
 FROM styles s JOIN style_groups g ON g.id = s.group_id
 LEFT JOIN languages le ON le.code = 'en'
 LEFT JOIN style_translations te ON te.style_id = s.id AND te.language_id = le.id
 LEFT JOIN languages lv ON lv.code = 'vi'
 LEFT JOIN style_translations tv ON tv.style_id = s.id AND tv.language_id = lv.id
 WHERE s.is_active = TRUE AND s.is_explore_enabled = TRUE
 """;
 private ExploreStyleResponse row(ResultSet r, int index) throws SQLException {
  return new ExploreStyleResponse(r.getLong("id"), r.getString("code"), r.getString("slug"),
   r.getString("name_en"), r.getString("name_vi"), r.getString("description_vi"),
   r.getString("description_en"), r.getString("image_url"),
   new ExploreGroupResponse(r.getLong("group_id"), r.getString("group_code"),
    r.getString("group_en"), r.getString("group_vi")));
 }
 public List<ExploreStyleResponse> styles() {
  return jdbc.query(SELECT + " ORDER BY g.sort_order, s.sort_order, s.id", this::row);
 }
 public java.util.Optional<ExploreStyleResponse> detail(String slug) {
  return jdbc.query(SELECT + " AND s.slug = ?", this::row, slug).stream().findFirst();
 }
 public java.util.Optional<ExploreStyleResponse> detailById(Long id) { return jdbc.query(SELECT + " AND s.id = ?", this::row, id).stream().findFirst(); }
 public List<ExploreGroupResponse> groups() {
  return jdbc.query("""
   SELECT g.id, g.code, g.name_en, g.name_vi FROM style_groups g
   WHERE EXISTS (SELECT 1 FROM styles s WHERE s.group_id=g.id
    AND s.is_active=TRUE AND s.is_explore_enabled=TRUE)
   ORDER BY g.sort_order, g.id
   """, (r, i) -> new ExploreGroupResponse(r.getLong("id"), r.getString("code"),
    r.getString("name_en"), r.getString("name_vi")));
 }
}
