package com.datn.fashion.service;
import com.datn.fashion.dto.wardrobe.OutfitRecommendationsResponse;
import com.datn.fashion.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
@Service
public class OutfitRecommendationService {
 private final WardrobeService wardrobe;
 private final StylePreferencesService preferences;
 public OutfitRecommendationService(WardrobeService wardrobe,StylePreferencesService preferences) {this.wardrobe=wardrobe;this.preferences=preferences;}
 @Transactional(readOnly=true)
 public OutfitRecommendationsResponse recommend(Long owner,int page,int limit) {
  if(page<0 || page>119 || limit<1 || limit>12) throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_PAGINATION","page phải từ 0–119 và limit từ 1–12.");
  var items=wardrobe.list(owner,null,null,null);
  var options=wardrobe.options(owner);
  var prefs=preferences.get(owner);
  return OutfitRules.recommend(items,options,new HashSet<>(prefs.styleIds()),new HashSet<>(prefs.colorIds()),page,limit);
 }
}
