package com.datn.fashion.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;

/** Illustration metadata only: selectable catalog entries always come from MySQL. */
@Component
public class StylePresentation {
    public record Entry(Integer illustrationIndex, String descriptionVi, String descriptionEn) {}
    private final Map<String, Entry> entries;
    public StylePresentation(ObjectMapper mapper) throws IOException {
        try (var input = new ClassPathResource("style-presentation.json").getInputStream()) {
            entries = mapper.readValue(input, new TypeReference<Map<String, Entry>>() {});
        }
    }
    public Integer index(String code) { var entry = entries.get(code); return entry == null ? null : entry.illustrationIndex(); }
    public String description(String code, String language) {
        var entry = entries.get(code);
        return entry == null ? null : language.equals("en") ? entry.descriptionEn() : entry.descriptionVi();
    }
}
