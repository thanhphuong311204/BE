package com.datn.fashion;

import com.datn.fashion.dto.face.FaceAnalysisResponse;
import com.datn.fashion.entity.User;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.security.JwtService;
import com.datn.fashion.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:face_test;MODE=MySQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class FaceAnalysisIntegrationTest {
    @Autowired MockMvc mvc; @Autowired UserRepository users; @Autowired JwtService jwt;
    @Autowired org.springframework.security.oauth2.jwt.JwtEncoder encoder;
    @MockitoBean FaceInferenceClient inference;
    User user;String token;
    @BeforeEach void setup() {
        user=new User();user.email="face-test@example.com";user.passwordHash="test-hash";user=users.saveAndFlush(user);token=jwt.create(user.id);
        when(inference.analyze(any())).thenReturn(new FaceAnalysisResponse(FaceAnalysisResponse.Status.SUCCESS,FaceAnalysisResponse.Shape.HEART,null,"test-only-model",1.,List.of("EXPLORATORY")));
    }
    MockMultipartFile file() throws Exception {
        var out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB),"png",out);
        return new MockMultipartFile("file","face.png","image/png",out.toByteArray());
    }
    @Test void validJwtMultipartAndNullableConfidence() throws Exception {
        mvc.perform(multipart("/api/ai/face-analysis").file(file()).header("Authorization","Bearer "+token))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"))
            .andExpect(jsonPath("$.status").value("SUCCESS")).andExpect(jsonPath("$.faceShape").value("HEART"))
            .andExpect(jsonPath("$.confidence").value(org.hamcrest.Matchers.nullValue()));
        verify(inference).analyze(any());
    }
    @Test void missingAndInvalidJwt() throws Exception {
        mvc.perform(multipart("/api/ai/face-analysis").file(file())).andExpect(status().isUnauthorized());
        mvc.perform(multipart("/api/ai/face-analysis").file(file()).header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
        verifyNoInteractions(inference);
    }
    @Test void expiredJwtRejected() throws Exception {
        var now=java.time.Instant.now();
        var claims=org.springframework.security.oauth2.jwt.JwtClaimsSet.builder().issuer("fashion-api").subject(user.id.toString()).issuedAt(now.minusSeconds(7200)).expiresAt(now.minusSeconds(3600)).build();
        var header=org.springframework.security.oauth2.jwt.JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        var expired=encoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(header,claims)).getTokenValue();
        mvc.perform(multipart("/api/ai/face-analysis").file(file()).header("Authorization","Bearer "+expired)).andExpect(status().isUnauthorized());
        verifyNoInteractions(inference);
    }
    @Test void inactiveAndDeletedAccount() throws Exception {
        user.status=User.Status.SUSPENDED;users.saveAndFlush(user);
        mvc.perform(multipart("/api/ai/face-analysis").file(file()).header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        user.status=User.Status.ACTIVE;user.deletedAt=java.time.Instant.now();users.saveAndFlush(user);
        mvc.perform(multipart("/api/ai/face-analysis").file(file()).header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        verifyNoInteractions(inference);
    }
    @Test void badAndOversizedInputNeverForwarded() throws Exception {
        mvc.perform(multipart("/api/ai/face-analysis").file(new MockMultipartFile("file","a.jpg","image/jpeg",new byte[]{1,2})).header("Authorization","Bearer "+token))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_IMAGE"));
        mvc.perform(multipart("/api/ai/face-analysis").file(new MockMultipartFile("file","a.jpg","image/jpeg",new byte[5*1024*1024+1])).header("Authorization","Bearer "+token))
            .andExpect(status().isPayloadTooLarge());verifyNoInteractions(inference);
    }
    @Test void qualityStatusesPreserved() throws Exception {
        for(var s:List.of(FaceAnalysisResponse.Status.NO_FACE,FaceAnalysisResponse.Status.MULTIPLE_FACES,FaceAnalysisResponse.Status.LOW_QUALITY)) {
            when(inference.analyze(any())).thenReturn(new FaceAnalysisResponse(s,null,null,"test-model",1.,List.of()));
            mvc.perform(multipart("/api/ai/face-analysis").file(file()).header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value(s.name()));
        }
    }
}
