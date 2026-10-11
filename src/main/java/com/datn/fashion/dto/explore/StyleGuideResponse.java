package com.datn.fashion.dto.explore;
import java.util.List;

/** Curated editorial guidance, never an AI recommendation. */
public record StyleGuideResponse(Overview overview, List<Note> recognition,
 List<PaletteColor> palette, List<Note> garments, List<Note> accessories,
 List<Outfit> outfits, List<Note> occasions, List<Note> suitability,
 List<Note> mistakes, Starter gettingStarted, List<Source> sources) {
 public record Overview(String definition,String origin,String spirit,String context) {}
 public record Note(String title,String detail) {}
 public record PaletteColor(String name,String hex,String combination) {}
 public record Outfit(String key,String name,List<String> items,String explanation,
  List<String> occasions,String imageUrl) {}
 public record Starter(List<Note> essentials,List<Note> steps) {}
 public record Source(String title,String url,String supports) {}
}
