package com.datn.fashion.repository;
import com.datn.fashion.dto.wardrobe.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.*;
@Repository
public class WardrobeRepository {
 private final JdbcTemplate jdbc;
 public WardrobeRepository(JdbcTemplate jdbc) {this.jdbc=jdbc;}
 public List<WardrobeOptionsResponse.Category> categories() {
  return jdbc.query("""
   SELECT c.id,c.parent_id,c.code,COALESCE(v.name,e.name,c.code) name,e.name name_en
   FROM clothing_categories c
   LEFT JOIN clothing_category_translations v ON v.category_id=c.id AND v.language_id=(SELECT id FROM languages WHERE code='vi')
   LEFT JOIN clothing_category_translations e ON e.category_id=c.id AND e.language_id=(SELECT id FROM languages WHERE code='en')
   WHERE c.is_active=TRUE ORDER BY c.sort_order,c.id
   """,(r,n)->new WardrobeOptionsResponse.Category(r.getLong("id"),r.getObject("parent_id",Long.class),r.getString("code"),r.getString("name"),r.getString("name_en")));
 }
 public List<WardrobeOptionsResponse.Color> colors() {
  return jdbc.query("""
   SELECT c.id,c.code,c.hex_code,COALESCE(v.name,c.code) name FROM colors c
   LEFT JOIN color_translations v ON v.color_id=c.id AND v.language_id=(SELECT id FROM languages WHERE code='vi')
   WHERE c.is_active=TRUE ORDER BY c.sort_order,c.id
   """,(r,n)->new WardrobeOptionsResponse.Color(r.getLong("id"),r.getString("code"),r.getString("name"),r.getString("hex_code")));
 }
 public List<WardrobeItemResponse> list(Long owner,Long main,Long sub,Long color,Long id) {
  var sql=new StringBuilder("SELECT * FROM wardrobe_items WHERE user_id=?");var args=new ArrayList<Object>();args.add(owner);
  if(main!=null){sql.append(" AND main_category_id=?");args.add(main);}
  if(sub!=null){sql.append(" AND sub_category_id=?");args.add(sub);}
  if(color!=null){sql.append(" AND color_id=?");args.add(color);}
  if(id!=null){sql.append(" AND id=?");args.add(id);}
  sql.append(" ORDER BY created_at DESC,id DESC");
  var rows=jdbc.query(sql.toString(),(r,n)->new WardrobeItemResponse(r.getLong("id"),r.getString("name"),
   r.getLong("main_category_id"),r.getObject("sub_category_id",Long.class),r.getLong("color_id"),
   r.getString("image_url"),r.getString("notes"),new ArrayList<>(),r.getTimestamp("created_at").toLocalDateTime(),
   r.getTimestamp("updated_at").toLocalDateTime()),args.toArray());
  if(!rows.isEmpty()){
   var links=new HashMap<Long,List<Long>>();
   String marks=String.join(",",Collections.nCopies(rows.size(),"?"));
   jdbc.query("SELECT item_id,style_id FROM wardrobe_item_styles WHERE item_id IN ("+marks+") ORDER BY style_id",
    (org.springframework.jdbc.core.RowCallbackHandler)r->links.computeIfAbsent(r.getLong("item_id"),key->new ArrayList<>()).add(r.getLong("style_id")),
    rows.stream().map(WardrobeItemResponse::id).toArray());
   for(var row:rows)row.styleIds().addAll(links.getOrDefault(row.id(),List.of()));
  }
  return rows;
 }
 public Long create(Long owner,WardrobeItemRequest data,String url,String publicId) {
  var key=new GeneratedKeyHolder();
  jdbc.update(c->{
   var p=c.prepareStatement("INSERT INTO wardrobe_items(user_id,name,main_category_id,sub_category_id,color_id,image_url,image_public_id,notes) VALUES(?,?,?,?,?,?,?,?)",new String[]{"id"});
   p.setLong(1,owner);p.setString(2,data.name().trim());p.setLong(3,data.mainCategoryId());
   p.setObject(4,data.subCategoryId());p.setLong(5,data.colorId());p.setString(6,url);p.setString(7,publicId);p.setString(8,data.notes());return p;
  },key);Long id=Objects.requireNonNull(key.getKey()).longValue();replaceStyles(id,data.styleIds());return id;
 }
 public void update(Long owner,Long id,WardrobeItemRequest d) {
  jdbc.update("UPDATE wardrobe_items SET name=?,main_category_id=?,sub_category_id=?,color_id=?,notes=?,updated_at=CURRENT_TIMESTAMP WHERE id=? AND user_id=?",
   d.name().trim(),d.mainCategoryId(),d.subCategoryId(),d.colorId(),d.notes(),id,owner);replaceStyles(id,d.styleIds());
 }
 private void replaceStyles(Long id,List<Long> styles) {
  jdbc.update("DELETE FROM wardrobe_item_styles WHERE item_id=?",id);
  if(styles!=null) for(Long style:styles) jdbc.update("INSERT INTO wardrobe_item_styles(item_id,style_id) VALUES(?,?)",id,style);
 }
 public String publicId(Long owner,Long id) {return jdbc.queryForObject("SELECT image_public_id FROM wardrobe_items WHERE user_id=? AND id=?",String.class,owner,id);}
 public void delete(Long owner,Long id) {jdbc.update("DELETE FROM wardrobe_items WHERE user_id=? AND id=?",owner,id);}
}
