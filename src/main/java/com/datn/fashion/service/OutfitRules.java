package com.datn.fashion.service;

import com.datn.fashion.dto.wardrobe.*;
import com.datn.fashion.dto.wardrobe.OutfitRecommendationsResponse.Outfit;
import java.util.*;
import java.util.stream.Collectors;

/** Deterministic metadata heuristics; no image analysis or external AI calls. */
public final class OutfitRules {
 private OutfitRules() {}
 private static final Comparator<Outfit> ORDER = Comparator.comparingInt(Outfit::score).reversed().thenComparing(Outfit::id);
 // Only the best 120 outfits are exposed. Memory stays bounded for large wardrobes.
 public static final int MAX_RESULTS = 120;
 public static OutfitRecommendationsResponse recommend(List<WardrobeItemResponse> input,
     WardrobeOptionsResponse options, Set<Long> preferredStyles, Set<Long> preferredColors, int page, int limit) {
  var categories = options.categories().stream().collect(Collectors.toMap(WardrobeOptionsResponse.Category::id, c->c));
  var colors = options.colors().stream().collect(Collectors.toMap(WardrobeOptionsResponse.Color::id, c->c));
  var groups = new HashMap<String,List<WardrobeItemResponse>>();
  var seenItems = new HashSet<Long>(); int ambiguous = 0;
  for(var item:input.stream().sorted(Comparator.comparing(WardrobeItemResponse::id)).toList()) {
   if(!seenItems.add(item.id())) continue;
   var main=categories.get(item.mainCategoryId());
   if(main==null || main.parentId()!=null) continue;
   String kind=main.code();
   if(item.subCategoryId()!=null) {
    var sub=categories.get(item.subCategoryId());
    if(sub==null || !Objects.equals(sub.parentId(),main.id())) continue;
    if(kind.equals("DRESSES_SKIRTS")) kind=sub.code();
   }
   if(kind.equals("DRESSES_SKIRTS")) {ambiguous++;continue;}
   groups.computeIfAbsent(kind,k->new ArrayList<>()).add(item);
  }
  var heap=new PriorityQueue<Outfit>(ORDER.reversed());
  var tops=groups.getOrDefault("TOPS",List.of());
  var lowers=new ArrayList<>(groups.getOrDefault("BOTTOMS",List.of()));
  lowers.addAll(groups.getOrDefault("DRESSES_SKIRTS_SKIRT",List.of()));
  for(var top:tops) for(var bottom:lowers) offer(heap,build(List.of(top,bottom),categories.get(bottom.mainCategoryId()).code().equals("DRESSES_SKIRTS")?"Áo và chân váy":"Áo và quần",groups,colors,preferredStyles,preferredColors));
  for(var dress:groups.getOrDefault("DRESSES_SKIRTS_DRESS",List.of())) offer(heap,build(List.of(dress),"Phối cùng đầm",groups,colors,preferredStyles,preferredColors));
  var ranked=heap.stream().sorted(ORDER).toList();
  int start=Math.min(page*limit,ranked.size()), end=Math.min(start+limit,ranked.size());
  var messages=new ArrayList<String>();
  if(ambiguous>0) messages.add("Có "+ambiguous+" món cần xác nhận loại con Đầm hoặc Chân váy trước khi phối.");
  if(ranked.isEmpty()) messages.add(input.isEmpty()?"Tủ đồ đang trống. Hãy thêm trang phục để bắt đầu.":"Cần một chiếc đầm đã xác định loại con, hoặc áo cùng quần / chân váy để phối đồ.");
  if(ranked.size()==MAX_RESULTS) messages.add("Hiển thị tối đa 120 tổ hợp được xếp hạng cao nhất.");
  return new OutfitRecommendationsResponse("rule_based_v1",ranked.isEmpty()?"INSUFFICIENT_DATA":"READY",List.copyOf(messages),page,limit,end<ranked.size(),ranked.subList(start,end));
 }
 private static void offer(PriorityQueue<Outfit> heap, Outfit outfit) {
  heap.offer(outfit); if(heap.size()>MAX_RESULTS) heap.poll();
 }
 private static Outfit build(List<WardrobeItemResponse> core,String name,Map<String,List<WardrobeItemResponse>> groups,
    Map<Long,WardrobeOptionsResponse.Color> colors,Set<Long> styles,Set<Long> favoriteColors) {
  var pieces=new ArrayList<>(core);
  for(String optional:List.of("OUTERWEAR","SHOES","BAGS","ACCESSORIES")) {
   var best=groups.getOrDefault(optional,List.of()).stream().min(Comparator
     .<WardrobeItemResponse>comparingInt(i->itemScore(i,styles,favoriteColors)+core.stream().mapToInt(c->harmony(i,c,colors)).sum()).reversed()
     .thenComparing(WardrobeItemResponse::id));
   best.ifPresent(pieces::add);
  }
  var reasons=new ArrayList<String>();
  reasons.add(core.size()==1?"Đầm là trang phục liền thân; không ghép thêm quần hoặc chân váy.":"Kết hợp áo với một món mặc dưới, phân biệt chân váy với đầm.");
  int score=pieces.stream().mapToInt(i->itemScore(i,styles,favoriteColors)).sum();
  int harmony=0;
  for(int a=0;a<pieces.size();a++) for(int b=a+1;b<pieces.size();b++) harmony+=harmony(pieces.get(a),pieces.get(b),colors);
  score+=harmony;
  if(harmony>0) reasons.add("Có cặp màu trung tính, gần tông hoặc bổ túc theo quy tắc màu cơ bản.");
  if(pieces.stream().anyMatch(i->i.styleIds().stream().anyMatch(styles::contains))) reasons.add("Có món mang phong cách bạn đã chọn trong sở thích.");
  if(pieces.stream().anyMatch(i->favoriteColors.contains(i.colorId()))) reasons.add("Có màu nằm trong bảng màu yêu thích của bạn.");
  if(pieces.size()>core.size()) reasons.add("Phụ kiện và các món bổ sung đều có sẵn trong tủ đồ của bạn.");
  reasons.add("Gợi ý dựa trên danh mục và màu đã lưu; hãy tự kiểm tra chất liệu, phom dáng và hoàn cảnh sử dụng.");
  String id=pieces.stream().map(WardrobeItemResponse::id).sorted().map(String::valueOf).collect(Collectors.joining("-"));
  return new Outfit(id,name,List.copyOf(pieces),score,List.copyOf(reasons));
 }
 private static int itemScore(WardrobeItemResponse i,Set<Long> styles,Set<Long> colors) {
  return (int)i.styleIds().stream().filter(styles::contains).count()*6+(colors.contains(i.colorId())?4:0);
 }
 static int harmony(WardrobeItemResponse a,WardrobeItemResponse b,Map<Long,WardrobeOptionsResponse.Color> colors) {
  var x=colors.get(a.colorId());var y=colors.get(b.colorId());
  double[] first=x==null?null:hsv(x.hexCode()),second=y==null?null:hsv(y.hexCode());
  if(first==null || second==null) return 0;
  if(first[1]<.18 || second[1]<.18 || first[2]<.2 || second[2]<.2) return 10;
  double delta=Math.abs(first[0]-second[0]);delta=Math.min(delta,360-delta);
  return delta<=40?6:delta>=150?4:0;
 }
 private static double[] hsv(String hex) {
  if(hex==null || !hex.matches("#[0-9a-fA-F]{6}")) return null;
  int rgb=Integer.parseInt(hex.substring(1),16);
  double r=((rgb>>16)&255)/255.0,g=((rgb>>8)&255)/255.0,b=(rgb&255)/255.0;
  double max=Math.max(r,Math.max(g,b)),min=Math.min(r,Math.min(g,b)),d=max-min,h=0;
  if(d>0) {if(max==r) h=((g-b)/d)%6;else if(max==g) h=(b-r)/d+2;else h=(r-g)/d+4;h=(h*60+360)%360;}
  return new double[]{h,max==0?0:d/max,max};
 }
}
