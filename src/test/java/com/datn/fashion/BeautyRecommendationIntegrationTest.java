package com.datn.fashion;

import com.datn.fashion.entity.User;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.security.JwtService;
import com.datn.fashion.service.FaceInferenceClient;
import com.datn.fashion.dto.face.FaceAnalysisResponse.Shape;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:beauty_test;MODE=MySQL;DB_CLOSE_DELAY=-1","debug=false","logging.level.root=WARN","logging.level.org.springframework=WARN","logging.level.org.hibernate=WARN"})
@AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class BeautyRecommendationIntegrationTest {
    @Autowired MockMvc mvc;@Autowired UserRepository users;@Autowired JwtService jwt;@Autowired ObjectMapper mapper;
    @MockitoBean FaceInferenceClient inference;
    String token;User user;
    @BeforeEach void setup(){user=new User();user.email="beauty-test@example.com";user.passwordHash="test-only";user=users.saveAndFlush(user);token=jwt.create(user.id);}
    @Test void allFiveShapesHaveMultipleChoicesForEachType() throws Exception {
        for(var shape:Shape.values())for(var type:new String[]{"HAIRSTYLE","MAKEUP"}) {
            var result=mvc.perform(get("/api/beauty/recommendations").param("faceShape",shape.name()).param("type",type).header("Authorization","Bearer "+token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mechanism").value("FACE_SHAPE_RULE_BASED")).andExpect(jsonPath("$.confirmedFaceShape").value(shape.name())).andReturn();
            var items=mapper.readTree(result.getResponse().getContentAsString()).get("items");assertTrue(items.size()>=2);
            for(var item:items){assertEquals(type,item.get("type").asText());assertFalse(item.get("reason").asText().isBlank());if(!item.get("thumbnail").isNull()){assertTrue(item.get("thumbnail").get("url").asText().startsWith("https://res.cloudinary.com/"));assertFalse(item.get("thumbnail").get("attribution").asText().isBlank());assertEquals("AVAILABLE",item.get("imageStatus").asText());}else{assertEquals("MISSING_LICENSED_IMAGE",item.get("imageStatus").asText());}assertTrue(item.get("steps").size()>=3);assertTrue(item.get("matchesConfirmedShape").asBoolean());}
        }
        verifyNoInteractions(inference);
    }
    @Test void explorationAndDetailKeepSelectedShape() throws Exception {
        var result=mvc.perform(get("/api/beauty/recommendations").param("faceShape","SQUARE").param("scope","ALL").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andReturn();
        assertEquals(41,mapper.readTree(result.getResponse().getContentAsString()).get("items").size());
        mvc.perform(get("/api/beauty/recommendations/hair-long-layers").param("faceShape","SQUARE").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.confirmedFaceShape").value("SQUARE")).andExpect(jsonPath("$.item.id").value("hair-long-layers")).andExpect(jsonPath("$.item.matchesConfirmedShape").value(false));
    }
    @Test void validatesFiltersMissingShapeAndUnknownId() throws Exception {
        for(String q:new String[]{"faceShape=DIAMOND","faceShape=OVAL&type=OTHER","faceShape=OVAL&scope=BAD","type=MAKEUP"})
            mvc.perform(get("/api/beauty/recommendations?"+q).header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/beauty/recommendations/unknown").param("faceShape","OVAL").header("Authorization","Bearer "+token)).andExpect(status().isNotFound());
    }
    @Test void authenticationAndAccountStillRequired() throws Exception {
        mvc.perform(get("/api/beauty/recommendations?faceShape=HEART")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/beauty/recommendations?faceShape=HEART").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
        user.status=User.Status.SUSPENDED;users.saveAndFlush(user);
        mvc.perform(get("/api/beauty/recommendations?faceShape=HEART").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
    }
}
