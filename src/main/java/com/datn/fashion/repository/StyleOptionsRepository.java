package com.datn.fashion.repository;

import com.datn.fashion.dto.style.StyleOptionResponse;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class StyleOptionsRepository {
    public enum Catalog {
        STYLE("styles", "style_translations", "style_id", false),
        COLOR("colors", "color_translations", "color_id", true),
        OCCASION("occasions", "occasion_translations", "occasion_id", false);
        final String table, translations, foreignKey;
        final boolean color;
        Catalog(String table, String translations, String foreignKey, boolean color) {
            this.table = table; this.translations = translations; this.foreignKey = foreignKey; this.color = color;
        }
    }

    private final EntityManager entityManager;
    private final com.datn.fashion.service.StylePresentation presentation;
    public StyleOptionsRepository(EntityManager entityManager, com.datn.fashion.service.StylePresentation presentation) { this.entityManager = entityManager; this.presentation = presentation; }

    @SuppressWarnings("unchecked")
    public List<StyleOptionResponse> options(Catalog c, String language) {
        // Table names come only from the fixed enum; external input is parameterized.
        String sql = "SELECT c.id, c.code, COALESCE(t.name, c.code), "
            + (c.color ? "NULL, c.hex_code" : "t.description, NULL")
            + " FROM " + c.table + " c"
            + " LEFT JOIN languages l ON l.code = :language AND l.is_active = TRUE"
            + " LEFT JOIN " + c.translations + " t ON t." + c.foreignKey
            + " = c.id AND t.language_id = l.id"
            + " WHERE c.is_active = TRUE ORDER BY c.sort_order, c.id";
        List<Object[]> rows = entityManager.createNativeQuery(sql).setParameter("language", language).getResultList();
        return rows.stream().map(r -> new StyleOptionResponse(((Number) r[0]).longValue(),
            (String) r[1], (String) r[2], r[3] == null && c == Catalog.STYLE ? presentation.description((String) r[1], language) : (String) r[3], (String) r[4], c == Catalog.STYLE ? presentation.index((String) r[1]) : null)).toList();
    }

    public boolean allActive(Catalog c, List<Long> ids) {
        // Lock referenced rows until the save commits: prevents deactivation/deletion
        // from racing the active-ID validation. H2 tests use its FOR UPDATE support.
        var rows = entityManager.createNativeQuery("SELECT id FROM " + c.table
            + " WHERE is_active = TRUE AND id IN (:ids) FOR UPDATE")
            .setParameter("ids", ids).getResultList();
        return rows.size() == ids.size();
    }
}
