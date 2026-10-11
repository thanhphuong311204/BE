package com.datn.fashion.dto.explore;
import java.util.List;
public record ExploreCatalogResponse(List<ExploreStyleResponse> styles, List<ExploreGroupResponse> groups) {}
