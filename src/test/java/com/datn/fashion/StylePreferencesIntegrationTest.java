package com.datn.fashion;

import com.datn.fashion.entity.User;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.service.AvatarStorage;
import com.datn.fashion.service.StylePreferencesService;
import com.datn.fashion.dto.style.SaveStylePreferencesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.time.Instant;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class StylePreferencesIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired JdbcTemplate jdbc;
    @Autowired StylePreferencesService service;
    @Autowired PlatformTransactionManager transactions;
    // Same mock declarations as existing Auth/Profile tests: reuse the test context.
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    com.datn.fashion.repository.UserProfileRepository profiles;
    @MockitoBean AvatarStorage avatars;
    Long userId;

    @BeforeEach void setup() {
        profiles.deleteAll(); users.deleteAll();
        for (String table : List.of("style_translations", "color_translations", "occasion_translations", "styles", "colors", "occasions", "languages"))
            jdbc.update("DELETE FROM " + table);
        var user = new User(); user.email = "onboarding@example.com"; user.passwordHash = "fixture";
        userId = users.saveAndFlush(user).id;
        jdbc.update("INSERT INTO languages(id,code,name,native_name,is_active) VALUES(10,'vi','Vietnamese','Tiếng Việt',TRUE),(20,'en','English','English',TRUE)");
        jdbc.update("INSERT INTO styles(id,code,is_active,sort_order) VALUES(101,'MINIMALIST',TRUE,2),(102,'OLD_MONEY',TRUE,1),(103,'VINTAGE',TRUE,3),(104,'FEMININE',TRUE,4),(105,'INACTIVE',FALSE,5)");
        jdbc.update("INSERT INTO colors(id,code,hex_code,is_active,sort_order) VALUES(201,'IVORY','#FAF8F4',TRUE,1),(202,'BLACK','#191816',TRUE,2),(203,'OFF',NULL,FALSE,3)");
        jdbc.update("INSERT INTO occasions(id,code,is_active,sort_order) VALUES(301,'WORK',TRUE,1),(302,'SCHOOL',TRUE,2),(303,'OFF',FALSE,3)");
        jdbc.update("INSERT INTO style_translations(style_id,language_id,name) VALUES(101,10,'Tối giản'),(101,20,'Minimalist'),(102,10,'Cổ điển sang trọng')");
        jdbc.update("INSERT INTO color_translations(color_id,language_id,name) VALUES(201,10,'Trắng ngà')");
        jdbc.update("INSERT INTO occasion_translations(occasion_id,language_id,name) VALUES(301,10,'Đi làm')");
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor token(Long id) {
        return jwt().jwt(j -> j.subject(id.toString()));
    }
    private Map<String,Object> selections() {
        return Map.of("styleIds",List.of(101,102,104),"colorIds",List.of(201),"occasionIds",List.of(301));
    }
    private ResultActions save(Object body) throws Exception {
        return mvc.perform(put("/api/style-preferences").with(token(userId))
            .contentType("application/json").content(json.writeValueAsString(body)));
    }
    private ResultActions getPreferences() throws Exception {
        return mvc.perform(get("/api/style-preferences").with(token(userId)));
    }

    @Test void allEndpointsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/style-preferences")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/style-preferences/options")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/style-preferences").contentType("application/json").content(json.writeValueAsString(selections()))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/style-preferences/skip")).andExpect(status().isUnauthorized());
    }
    @Test void readingNotStartedDoesNotCreateRows() throws Exception {
        getPreferences().andExpect(status().isOk()).andExpect(jsonPath("$.status").value("NOT_STARTED"))
            .andExpect(jsonPath("$.styleIds").isEmpty());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM user_style_preferences", Integer.class));
    }
    @Test void optionsAreActiveSortedTranslatedAndFallbackToCode() throws Exception {
        mvc.perform(get("/api/style-preferences/options").with(token(userId)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.styles.length()").value(4))
            .andExpect(jsonPath("$.styles[0].id").value(102))
            .andExpect(jsonPath("$.styles[0].name").value("Cổ điển sang trọng"))
            .andExpect(jsonPath("$.styles[2].name").value("VINTAGE"))
            .andExpect(jsonPath("$.colors[0].hexCode").value("#FAF8F4"))
            .andExpect(jsonPath("$.occasions[0].name").value("Đi làm"));
        mvc.perform(get("/api/style-preferences/options?language=en").with(token(userId)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.styles[1].name").value("Minimalist"));
        mvc.perform(get("/api/style-preferences/options?language=bad").with(token(userId))).andExpect(status().isBadRequest());
    }
    @Test void saveCompletesAndUpdateReplacesSelectionsWithoutDuplicates() throws Exception {
        save(selections()).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.completedAt").isNotEmpty()).andExpect(jsonPath("$.styleIds.length()").value(3));
        var replacement = Map.of("styleIds",List.of(103),"colorIds",List.of(202),"occasionIds",List.of(302));
        save(replacement).andExpect(status().isOk());
        save(replacement).andExpect(status().isOk());
        getPreferences().andExpect(jsonPath("$.styleIds[0]").value(103)).andExpect(jsonPath("$.colorIds[0]").value(202));
        for (String table : List.of("user_style_preferences","user_preferred_styles","user_preferred_colors","user_preferred_occasions"))
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class));
    }
    @Test void skipIsPersistentIdempotentAndCanBeCompletedLater() throws Exception {
        var first = mvc.perform(post("/api/style-preferences/skip").with(token(userId)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SKIPPED")).andReturn();
        var again = mvc.perform(post("/api/style-preferences/skip").with(token(userId))).andExpect(status().isOk()).andReturn();
        assertEquals(json.readTree(first.getResponse().getContentAsString()).get("skippedAt"),
            json.readTree(again.getResponse().getContentAsString()).get("skippedAt"));
        getPreferences().andExpect(jsonPath("$.status").value("SKIPPED"));
        save(selections()).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(post("/api/style-preferences/skip").with(token(userId)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.styleIds.length()").value(3));
    }
    @Test void validatesEmptyOversizedNullAndDuplicateIds() throws Exception {
        for (Object ids : List.of(List.of(), List.of(101,102,103,104), List.of(101,101), Arrays.asList(101,null), List.of(-1))) {
            var body = new HashMap<>(selections()); body.put("styleIds",ids);
            save(body).andExpect(status().isBadRequest());
        }
        for (String field : List.of("styleIds","colorIds","occasionIds")) {
            var body = new HashMap<>(selections()); body.remove(field);
            save(body).andExpect(status().isBadRequest());
        }
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM user_style_preferences", Integer.class));
    }
    @Test void invalidOrInactiveIdsDoNotChangeSavedPreferences() throws Exception {
        save(selections()).andExpect(status().isOk());
        for (var entry : Map.of("styleIds",105,"colorIds",203,"occasionIds",303).entrySet()) {
            var body = new HashMap<>(selections()); body.put(entry.getKey(),List.of(entry.getValue()));
            save(body).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields." + entry.getKey()).exists());
        }
        var body = new HashMap<>(selections()); body.put("colorIds",List.of(99999));
        save(body).andExpect(status().isBadRequest());
        getPreferences().andExpect(jsonPath("$.styleIds.length()").value(3)).andExpect(jsonPath("$.colorIds[0]").value(201));
    }
    @Test void ignoresNoClientIdentityAndKeepsUsersIsolated() throws Exception {
        save(selections()).andExpect(status().isOk());
        var other = new User(); other.email = "other@example.com"; other.passwordHash = "fixture";
        other = users.saveAndFlush(other);
        mvc.perform(get("/api/style-preferences").with(token(other.id)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("NOT_STARTED"));
        var forged = new HashMap<>(selections()); forged.put("userId", other.id);
        save(forged).andExpect(status().isBadRequest());
    }
    @Test void inactiveDeletedMissingAndMalformedSubjectsAreRejected() throws Exception {
        var user = users.findById(userId).orElseThrow(); user.status = User.Status.SUSPENDED; users.saveAndFlush(user);
        getPreferences().andExpect(status().isUnauthorized());
        user.status = User.Status.ACTIVE; user.deletedAt = Instant.now(); users.saveAndFlush(user);
        save(selections()).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/style-preferences/skip").with(token(99999L))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/style-preferences").with(jwt().jwt(j -> j.subject("bad")))).andExpect(status().isUnauthorized());
    }
    @Test void failureRollsBackStatusAndAllThreeCollections() throws Exception {
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactions).execute(status -> {
            service.save(userId, new SaveStylePreferencesRequest(List.of(101L),List.of(201L),List.of(301L)));
            throw new IllegalStateException("fixture rollback");
        }));
        for (String table : List.of("user_style_preferences","user_preferred_styles","user_preferred_colors","user_preferred_occasions"))
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class));
    }

    @Test void descriptionsPreferDatabaseAndIllustrationsDoNotInventCatalogEntries() throws Exception {
        mvc.perform(get("/api/style-preferences/options").with(token(userId)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.styles.length()").value(4))
            .andExpect(jsonPath("$.styles[0].illustrationIndex").value(6))
            .andExpect(jsonPath("$.styles[1].description").isNotEmpty());
        jdbc.update("UPDATE style_translations SET description='Mô tả từ database' WHERE style_id=101 AND language_id=10");
        mvc.perform(get("/api/style-preferences/options").with(token(userId)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.styles[1].description").value("Mô tả từ database"));
    }
    @Test void validatesSixColorAndFiveOccasionLimits() throws Exception {
        var tooManyColors=new HashMap<>(selections());
        tooManyColors.put("colorIds",List.of(1,2,3,4,5,6,7));
        save(tooManyColors).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.colorIds").exists());
        var tooManyOccasions=new HashMap<>(selections());
        tooManyOccasions.put("occasionIds",List.of(1,2,3,4,5,6));
        save(tooManyOccasions).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.occasionIds").exists());
    }
}
