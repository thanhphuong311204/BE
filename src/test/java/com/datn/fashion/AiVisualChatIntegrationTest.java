package com.datn.fashion;

import com.datn.fashion.service.*;
import com.datn.fashion.dto.ai.*;
import com.datn.fashion.entity.User;
import com.datn.fashion.repository.UserRepository;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:visual_chat;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.sql.init.schema-locations=classpath:schema-test.sql,classpath:schema-chat-test.sql","app.cloudinary.cloud-name=test-cloud"})
@AutoConfigureMockMvc @ActiveProfiles("test")
class AiVisualChatIntegrationTest {
    @Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired JdbcTemplate jdbc;
    @Autowired ChatVisualCatalog catalog;@Autowired ChatHistoryStore history;
    @MockitoBean GeminiClient gemini;@MockitoBean UserRepository users;
    final String image="https://res.cloudinary.com/test-cloud/image/upload/v1/nova/minimalist/outfit/a.png";
    @BeforeEach void setup() throws Exception {
        jdbc.update("DELETE FROM chat_conversations");jdbc.update("DELETE FROM users");jdbc.update("DELETE FROM style_visual_profiles");
        jdbc.update("DELETE FROM style_outfit_images");jdbc.update("DELETE FROM style_guides");jdbc.update("DELETE FROM style_translations");jdbc.update("DELETE FROM styles");jdbc.update("DELETE FROM style_groups");jdbc.update("DELETE FROM languages");
        for(long id:List.of(1L,2L)) {
            jdbc.update("INSERT INTO users(id,email,password_hash,status) VALUES (?,?,?,'ACTIVE')",id,"nova-visual-"+id+"@example.invalid","test-placeholder");
            var user=new User();user.id=id;user.status=User.Status.ACTIVE;when(users.findById(id)).thenReturn(Optional.of(user));
        }
        jdbc.update("INSERT INTO languages(id,code,name,native_name) VALUES (1,'vi','Vietnamese','Tiếng Việt')");
        jdbc.update("INSERT INTO style_groups(id,code,name_en,name_vi) VALUES (1,'casual','Casual','Thường ngày')");
        jdbc.update("INSERT INTO styles(id,code,slug,group_id,is_explore_enabled,image_url) VALUES (901,'MINIMALIST','minimalist',1,TRUE,?)","https://res.cloudinary.com/test-cloud/image/upload/v1/hero.png");
        jdbc.update("INSERT INTO style_translations(style_id,language_id,name) VALUES (901,1,'Tối giản')");
        String guide="""
            {"outfits":[{"key":"campus-1","name":"Đi học nhẹ nhàng","explanation":"Sơ mi và quần jeans gọn gàng.",
            "items":["Sơ mi ivory","Jeans xanh"],"occasions":["Đi học"]}]}
            """;
        jdbc.update("INSERT INTO style_guides(style_id,language_id,content) VALUES (901,1,?)",guide);
        jdbc.update("INSERT INTO style_outfit_images(style_id,outfit_key,image_url,public_id,sha256,provenance) VALUES (901,'campus-1',?,?,?,'Reviewed existing asset')",image,"nova/minimalist/outfit/a","a".repeat(64));
        String item="https://res.cloudinary.com/test-cloud/image/upload/v1/nova/minimalist/product/shirt.png";
        jdbc.update("INSERT INTO style_visual_profiles(style_id,content,provenance) VALUES (901,?,?)",
            json.writeValueAsString(Map.of("dna",List.of(Map.of("key","shirt","name","Sơ mi ivory","imageUrl",item)))),
            json.writeValueAsString(Map.of("shirt",Map.of("reviewed",true,"sha256","b".repeat(64),"imageUrl",item))));
        when(gemini.model()).thenReturn("gemini-3.8-flash");
    }
    JsonNode chatJson(long user,Map<String,Object> body) throws Exception {
        return json.readTree(mvc.perform(post("/api/ai/chat").with(jwt().jwt(j->j.subject(Long.toString(user)))).contentType("application/json")
            .content(json.writeValueAsString(body))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    @Test void imageOutfitPalettePersistReplayAndFollowupUseVerifiedData() throws Exception {
        when(gemini.generate(anyString(),any())).thenReturn("""
            {"text":"## Outfit đi học\\nSơ mi ivory phối jeans.","blocks":[
            {"type":"outfit_suggestions","refs":["outfit:901:campus-1"],"imageUrl":"https://evil.invalid/other.png"},
            {"type":"color_palette","colors":[{"name":"Ivory","hex":"#F4EFE5"}]}]}
            """);
        var body=new HashMap<String,Object>(Map.of("message","Gợi ý outfit đi học","requestId",UUID.randomUUID().toString()));
        var response=chatJson(1,body);assertEquals(1,response.path("schemaVersion").asInt());
        assertEquals(image,response.path("blocks").path(1).path("outfits").path(0).path("imageUrl").asText());
        assertFalse(response.toString().contains("evil.invalid"));
        assertEquals(response,chatJson(1,body));verify(gemini,times(1)).generate(anyString(),any());
        long id=response.path("conversationId").asLong();
        var restored=history.messages(1L,id,null,50).items().getLast();
        assertEquals(image,restored.blocks().get(1).outfits().getFirst().imageUrl());
        assertEquals("#F4EFE5",restored.blocks().get(2).colors().getFirst().hex());
        when(gemini.generate(anyString(),any())).thenReturn("""
            {"text":"Đổi sang pastel nhé.","blocks":[{"type":"outfit_suggestions","ideas":[
            {"name":"Pastel đi học","items":["Sơ mi hồng nhạt","Jeans trắng"]}]}]}
            """);
        var followup=chatJson(1,Map.of("message","Đổi sang pastel đi","conversationId",id));
        assertTrue(followup.path("blocks").path(1).path("outfits").path(0).path("imageUrl").isMissingNode());
        verify(gemini).generate(anyString(),argThat(r->r.message().contains("pastel") && r.history().stream().anyMatch(t->t.text().contains("outfit:901:campus-1")&&t.text().contains("#F4EFE5"))));
        mvc.perform(get("/api/ai/conversations/"+id+"/messages").with(jwt().jwt(j->j.subject("2")))).andExpect(status().isNotFound());
        mvc.perform(get("/api/ai/conversations/"+id+"/messages")).andExpect(status().isUnauthorized());
    }
    @Test void greetingsGeneralKnowledgeAndStylesOutsideCatalogCanBePlainText() throws Exception {
        for(String question:List.of("Ê nay","Thủ đô Nhật Bản là gì?","Giải thích phong cách cyber fairy mới")) {
            when(gemini.generate(anyString(),any())).thenReturn("{\"text\":\"Mình đây, bạn nhé.\",\"blocks\":[]}");
            var reply=chatJson(1,Map.of("message",question));assertEquals(1,reply.path("blocks").size());
        }
        assertTrue(catalog.retrieve("Ê nay",List.of()).outfits().isEmpty());
        verify(gemini,times(3)).generate(argThat(p->p.contains("Không giới hạn kiến thức")),any());
    }
    @Test void unreviewedWrongCloudAndHeroImagesAreNeverUsedAsOutfits() {
        assertFalse(catalog.trustedUrl("https://res.cloudinary.com/other/image/upload/a.png"));
        assertFalse(catalog.trustedUrl("https://res.cloudinary.com.evil.invalid/test-cloud/image/upload/a.png"));
        assertFalse(catalog.trustedUrl("http://res.cloudinary.com/test-cloud/image/upload/a.png"));
        var c=catalog.retrieve("Outfit đi học",List.of());assertEquals(image,c.outfits().getFirst().imageUrl());
        assertEquals(0,c.items().size()); // An isolated shirt does not establish a school outfit.assertFalse(c.promptData().toString().contains("https://"));
        jdbc.update("UPDATE style_outfit_images SET provenance='unreviewed'");
        assertNull(catalog.retrieve("Outfit di hoc",List.of()).outfits().getFirst().imageUrl());
        jdbc.update("UPDATE style_outfit_images SET provenance='Reviewed existing asset'");
        jdbc.update("UPDATE style_outfit_images SET image_url='https://res.cloudinary.com/test-cloud/image/upload/v1/hero.png'");
        assertNull(catalog.retrieve("Outfit đi học",List.of()).outfits().getFirst().imageUrl());
        jdbc.update("UPDATE style_visual_profiles SET provenance='{}'");assertTrue(catalog.retrieve("Outfit đi học",List.of()).items().isEmpty());
    }
    @Test void schoolTopicPrioritizesPreppyOverEarlierStylesMentioningSchool() throws Exception {
        jdbc.update("INSERT INTO styles(id,code,slug,group_id,is_explore_enabled,sort_order) VALUES (902,'PREPPY','preppy',1,TRUE,99)");
        jdbc.update("INSERT INTO style_translations(style_id,language_id,name) VALUES (902,1,'Học đường')");
        jdbc.update("INSERT INTO style_guides(style_id,language_id,content) VALUES (902,1,?)",
            "{\"outfits\":[{\"key\":\"polo\",\"name\":\"Polo chino\",\"occasions\":[\"Cuối tuần\"],\"items\":[\"Polo\",\"Chino\"]}]}");
        String url=image.replace("/minimalist/outfit/a.png","/preppy/outfit/polo.png");
        jdbc.update("INSERT INTO style_outfit_images(style_id,outfit_key,image_url,public_id,sha256,provenance) VALUES (902,'polo',?,?,?,'Reviewed existing asset')",
            url,"nova/preppy/outfit/polo","b".repeat(64));
        var retrieved=catalog.retrieve("T muốn nhiều ảnh phong cách học đường hơn",List.of());
        assertEquals("outfit:902:polo",retrieved.outfits().getFirst().id());
        assertEquals(url,catalog.librarySuggestions("Outfit học đường",List.of(),retrieved).getFirst().outfits().getFirst().imageUrl());
        var next=catalog.retrieve("Thêm ảnh khác",List.of(new ChatRequest.Turn(ChatRequest.Role.user,"Outfit học đường")),Set.of(url));
        assertTrue(next.outfits().stream().noneMatch(o->url.equals(o.imageUrl())));
    }
    @Test void oldTextMessagesRemainReadableAndNoImageSuggestionsStillWork() {
        var legacy=history.append(1L,null,null,"old","old reply","legacy");
        var restored=history.messages(1L,legacy.conversationId(),null,50).items().getLast();
        assertEquals("old reply",restored.content());assertTrue(restored.blocks().isEmpty());
    }

    @Test void invalidJsonFailsWithoutSavingFakeReplyAndSameRequestCanBeRetried() throws Exception {
        String requestId=UUID.randomUUID().toString();
        Map<String,Object> body=Map.of("message","Xin chào","requestId",requestId);
        when(gemini.generate(anyString(),any())).thenReturn("{broken");
        mvc.perform(post("/api/ai/chat").with(jwt().jwt(j->j.subject("1"))).contentType("application/json")
            .content(json.writeValueAsString(body))).andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.code").value("AI_INVALID_RESPONSE"));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM chat_messages",Integer.class));
        when(gemini.generate(anyString(),any())).thenReturn("Chào bạn, mình là NOVA!");
        var reply=chatJson(1,body);
        assertEquals("Chào bạn, mình là NOVA!",reply.path("reply").asText());
        assertEquals(1,reply.path("blocks").size());
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM chat_messages",Integer.class));
        assertEquals(reply,chatJson(1,body));
        verify(gemini,times(2)).generate(anyString(),any());
    }

    @Test void busyAndTimeoutOfferLibraryWithoutSavingAiReplyThenManualRetryRecovers() throws Exception {
        for(var code:List.of("AI_PROVIDER_BUSY","AI_TIMEOUT","AI_PROVIDER_TIMEOUT")) {
            int status=code.equals("AI_PROVIDER_BUSY")?503:504;
            String rid=UUID.randomUUID().toString();
            Map<String,Object> body=Map.of("message","T muốn nhiều ảnh phong cách học đường hơn","requestId",rid);
            when(gemini.generate(anyString(),any())).thenThrow(new com.datn.fashion.exception.ApiException(
                org.springframework.http.HttpStatus.valueOf(status),code,"AI đang bận",Map.of("retryAfterSeconds","2")));
            clearInvocations(gemini);
            mvc.perform(post("/api/ai/chat").with(jwt().jwt(j->j.subject("1"))).contentType("application/json")
                .content(json.writeValueAsString(body))).andExpect(status().is(status))
                .andExpect(header().string("Retry-After","2"))
                .andExpect(jsonPath("$.code").value(code)).andExpect(jsonPath("$.reply").doesNotExist())
                .andExpect(jsonPath("$.library.source").value("nova_library"))
                .andExpect(jsonPath("$.library.blocks[0].type").value("style_gallery"))
                .andExpect(jsonPath("$.library.blocks[0].images[0].imageUrl").value(image));
            verify(gemini,times(1)).generate(anyString(),any());
            assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM chat_messages",Integer.class));
            assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM chat_requests WHERE request_id=?",Integer.class,rid));
            doReturn("{\"text\":\"Đã khôi phục\",\"blocks\":[]}").when(gemini).generate(anyString(),any());
            var recovered=chatJson(1,body);
            assertEquals(recovered,chatJson(1,body));
            assertEquals(2,history.messages(1L,recovered.path("conversationId").asLong(),null,50).items().size());
            history.delete(1L,recovered.path("conversationId").asLong());
        }
    }
    @Test void uncertainUnrelatedOrMissingImagesHaveOnlyErrorAndNoLibrary() throws Exception {
        when(gemini.generate(anyString(),any())).thenThrow(new com.datn.fashion.exception.ApiException(
            org.springframework.http.HttpStatus.GATEWAY_TIMEOUT,"AI_TIMEOUT","AI chậm"));
        for(String message:List.of("Thủ đô Nhật Bản là gì?","Thêm ảnh","Mai đi học mấy giờ?","Outfit công sở")) {
            mvc.perform(post("/api/ai/chat").with(jwt().jwt(j->j.subject("1"))).contentType("application/json")
                .content(json.writeValueAsString(Map.of("message",message))))
                .andExpect(status().isGatewayTimeout()).andExpect(jsonPath("$.library").doesNotExist());
        }
        jdbc.update("UPDATE style_outfit_images SET provenance='unreviewed'");
        mvc.perform(post("/api/ai/chat").with(jwt().jwt(j->j.subject("1"))).contentType("application/json")
            .content(json.writeValueAsString(Map.of("message","Outfit đi học"))))
            .andExpect(status().isGatewayTimeout()).andExpect(jsonPath("$.library").doesNotExist());
    }
    @Test void photoFollowupResolvesUserTopicAndExcludesImagesBeyondModelHistory() throws Exception {
        var data=catalog.retrieve("Outfit đi học",List.of());
        assertFalse(catalog.librarySuggestions("Thêm ảnh",List.of(new ChatRequest.Turn(ChatRequest.Role.user,"Outfit đi học")),data).isEmpty());
        assertTrue(catalog.librarySuggestions("Thêm ảnh",List.of(new ChatRequest.Turn(ChatRequest.Role.user,"Thủ đô Nhật Bản")),data).isEmpty());
        var blocks=List.of(new VisualBlock("outfit_suggestions",null,null,null,data.outfits(),null,null,null));
        var first=history.append(1L,null,null,"Outfit đi học","Outfit", "test",null,blocks);
        long id=first.conversationId();
        for(int i=0;i<7;i++)history.append(1L,id,history.context(1L,id).lastMessageId(),"Tiếp tục","Text","test");
        assertFalse(history.context(1L,id).history().stream().anyMatch(t->t.text().contains("outfit:901:campus-1")));
        assertTrue(history.displayedImages(1L,id).contains(image));
        assertTrue(catalog.retrieve("T muốn nhiều ảnh phong cách học đường hơn",List.of(),history.displayedImages(1L,id)).outfits().isEmpty());
        assertThrows(com.datn.fashion.exception.ApiException.class,()->history.displayedImages(2L,id));
    }
}
