package com.datn.fashion.service;
import com.datn.fashion.dto.wardrobe.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
class OutfitRulesTest {
 WardrobeOptionsResponse options() {return new WardrobeOptionsResponse(List.of(
  new WardrobeOptionsResponse.Category(1L,null,"TOPS","Áo","Tops"),
  new WardrobeOptionsResponse.Category(2L,null,"BOTTOMS","Quần","Bottoms"),
  new WardrobeOptionsResponse.Category(3L,null,"DRESSES_SKIRTS","Váy/Đầm","Dresses"),
  new WardrobeOptionsResponse.Category(4L,3L,"DRESSES_SKIRTS_DRESS","Đầm","Dress"),
  new WardrobeOptionsResponse.Category(5L,3L,"DRESSES_SKIRTS_SKIRT","Chân váy","Skirt"),
  new WardrobeOptionsResponse.Category(6L,null,"SHOES","Giày","Shoes")),List.of(
  new WardrobeOptionsResponse.Color(1L,"WHITE","Trắng","#FFFFFF"),new WardrobeOptionsResponse.Color(2L,"RED","Đỏ","#FF0000"),new WardrobeOptionsResponse.Color(3L,"GREEN","Lá","#00FF00")),List.of());}
 WardrobeItemResponse item(long id,long category,Long sub,long color,List<Long> styles) {return new WardrobeItemResponse(id,"Món "+id,category,sub,color,"https://res.cloudinary.com/test/"+id,null,styles,LocalDateTime.of(2026,1,1,0,0),LocalDateTime.of(2026,1,1,0,0));}
 OutfitRecommendationsResponse run(List<WardrobeItemResponse> items) {return OutfitRules.recommend(items,options(),Set.of(),Set.of(),0,12);}
 @Test void separatesDressSkirtAndDoesNotInventMissingItems() {
  var items=List.of(item(10,1,null,1,List.of()),item(11,2,null,2,List.of()),item(12,3,5L,1,List.of()),item(13,3,4L,1,List.of()));
  var result=run(items);assertEquals(3,result.outfits().size());
  var sets=result.outfits().stream().map(o->o.items().stream().map(WardrobeItemResponse::id).collect(java.util.stream.Collectors.toSet())).toList();
  assertTrue(sets.contains(Set.of(10L,11L)));assertTrue(sets.contains(Set.of(10L,12L)));assertTrue(sets.contains(Set.of(13L)));
  assertTrue(result.outfits().stream().allMatch(o->!o.reasons().isEmpty()));
 }
 @Test void ambiguousAndInvalidSubtypesAreNotGuessed() {
  var result=run(List.of(item(1,1,null,1,List.of()),item(2,3,null,1,List.of()),item(3,3,6L,1,List.of())));
  assertEquals("INSUFFICIENT_DATA",result.status());assertFalse(result.messages().isEmpty());assertTrue(result.outfits().isEmpty());
  assertEquals("INSUFFICIENT_DATA",run(List.of()).status());
 }
 @Test void prefersColorHarmonyThenUserPreferences() {
  var top=item(1,1,null,2,List.of());var neutral=item(2,2,null,1,List.of());var green=item(3,2,null,3,List.of(90L,91L));
  assertEquals("1-2",run(List.of(top,neutral,green)).outfits().getFirst().id());
  var preferred=OutfitRules.recommend(List.of(top,neutral,green),options(),Set.of(90L,91L),Set.of(3L),0,12);
  assertEquals("1-3",preferred.outfits().getFirst().id());
  assertTrue(preferred.outfits().stream().filter(o->o.id().equals("1-3")).findFirst().orElseThrow().reasons().stream().anyMatch(r->r.contains("phong cách")));
  assertEquals(10,OutfitRules.harmony(top,neutral,options().colors().stream().collect(java.util.stream.Collectors.toMap(WardrobeOptionsResponse.Color::id,c->c))));
 }
 @Test void deterministicDeduplicationPaginationAndOwnedExtras() {
  var top=item(1,1,null,1,List.of());var bottoms=List.of(item(2,2,null,1,List.of()),item(3,2,null,2,List.of()));var shoes=item(4,6,null,1,List.of());
  var items=new ArrayList<>(List.of(top,top,shoes));items.addAll(bottoms);
  var first=OutfitRules.recommend(items,options(),Set.of(),Set.of(),0,1);var second=OutfitRules.recommend(items,options(),Set.of(),Set.of(),1,1);
  assertTrue(first.hasMore());assertFalse(second.hasMore());assertNotEquals(first.outfits().getFirst().id(),second.outfits().getFirst().id());
  Collections.reverse(items);assertEquals(first,OutfitRules.recommend(items,options(),Set.of(),Set.of(),0,1));
  assertTrue(first.outfits().getFirst().items().stream().anyMatch(i->i.id()==4));
 }
 @Test void highestRankSurvivesBoundedResults() {
  var items=new ArrayList<WardrobeItemResponse>();items.add(item(1,1,null,1,List.of()));
  for(int i=2;i<140;i++)items.add(item(i,2,null,1,i==139?List.of(90L):List.of()));
  var result=OutfitRules.recommend(items,options(),Set.of(90L),Set.of(),0,12);
  assertEquals("1-139",result.outfits().getFirst().id());assertTrue(result.hasMore());
 }
}
