package com.datn.fashion.dto.style;
import java.util.List;
public record StyleOptionsResponse(List<StyleOptionResponse> styles, List<StyleOptionResponse> colors, List<StyleOptionResponse> occasions) {}
