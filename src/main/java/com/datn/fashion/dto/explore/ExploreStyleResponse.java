package com.datn.fashion.dto.explore;
public record ExploreStyleResponse(Long id, String code, String slug, String name, String nameVi,
 String description, String descriptionEn, String imageUrl, ExploreGroupResponse group) {}
