package com.datn.fashion;

import com.datn.fashion.entity.User;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.nio.file.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.io.ByteArrayOutputStream;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="NOVA_FACE_LIVE",matches="true")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
    "spring.datasource.url=jdbc:h2:mem:face_live;MODE=MySQL;DB_CLOSE_DELAY=-1", "debug=false", "logging.level.root=WARN", "logging.level.org.springframework=WARN", "logging.level.org.hibernate=WARN"})
@ActiveProfiles("test")
class FaceAnalysisLiveTest {
    @LocalServerPort int port;
    @Autowired UserRepository users; @Autowired JwtService jwt; @Autowired ObjectMapper mapper;
    @DynamicPropertySource static void config(DynamicPropertyRegistry r) {
        r.add("app.face-ai.url",()->System.getenv("FACE_AI_URL"));
        r.add("app.face-ai.key",()->System.getenv("FACE_AI_SERVICE_KEY"));
    }
    HttpResponse<String> upload(byte[] bytes,String token) throws Exception {
        String boundary="nova-face-live-fixture-boundary";
        var body=new ByteArrayOutputStream();
        body.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes());
        body.write(bytes);body.write(("\r\n--"+boundary+"--\r\n").getBytes());
        var req=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/ai/face-analysis"))
            .timeout(Duration.ofSeconds(25)).header("Content-Type","multipart/form-data; boundary="+boundary);
        if(token!=null)req.header("Authorization","Bearer "+token);
        return HttpClient.newHttpClient().send(req.POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
    }
    @Test void realPythonBackendAndFlutter() throws Exception {
        var user=new User();user.email="face-live-"+java.util.UUID.randomUUID()+"@example.com";user.passwordHash="test-only";user=users.saveAndFlush(user);
        String token=jwt.create(user.id);
        Path root=Path.of("D:/DATN/FE"),ai=root.resolve("nova_face_ai"),output=Path.of(System.getenv().getOrDefault("NOVA_FACE_TEST_OUTPUT",ai.resolve("task_10_integration").toString()));
        var bytes=Files.readAllBytes(Path.of("D:/DATN/AI/face-shape/training_set/Heart/heart (10).jpg"));
        var valid=upload(bytes,token);assertEquals(200,valid.statusCode());
        var data=mapper.readTree(valid.body());assertEquals("SUCCESS",data.get("status").asText());assertTrue(data.get("confidence").isNull());
        assertEquals(401,upload(bytes,null).statusCode());assertEquals(401,upload(bytes,"invalid").statusCode());
        assertEquals("NO_FACE",mapper.readTree(upload(Files.readAllBytes(ai.resolve("task_09_final_evaluation/fixtures/no_face.png")),token).body()).get("status").asText());
        assertEquals("MULTIPLE_FACES",mapper.readTree(upload(Files.readAllBytes(Path.of("D:/DATN/AI/face-shape/training_set/Heart/heart (728).jpg")),token).body()).get("status").asText());
        assertEquals(400,upload(new byte[]{1,2,3},token).statusCode());
        assertEquals(413,upload(new byte[5*1024*1024+1],token).statusCode());
        var process=new ProcessBuilder("cmd.exe","/c","D:\\flutter\\bin\\flutter.bat","test","test/face_analysis_live_test.dart","--reporter","expanded")
            .directory(root.toFile()).redirectErrorStream(true).redirectOutput(output.resolve("flutter-live.log").toFile());
        process.environment().put("NOVA_FACE_TEST_BASE_URL","http://127.0.0.1:"+port);
        process.environment().put("NOVA_FACE_TEST_JWT",token);
        var child=process.start();
        if(!child.waitFor(120,java.util.concurrent.TimeUnit.SECONDS)) { child.destroyForcibly();fail("Flutter live test timeout; see task10 flutter-live.log"); }
        assertEquals(0,child.exitValue(),"Flutter live UI -> Spring JWT -> real Python failed; see task10 flutter-live.log");
        Files.writeString(output.resolve("LIVE_E2E_RESULT.json"),"{\"status\":\"PASS\",\"scope\":\"Flutter widget runner real HTTP to embedded Spring/H2/JWT to live FastAPI/MobileNetV3, no transport mocks\",\"physical_camera\":\"NOT_TESTED\"}");
        users.deleteById(user.id);
    }
}
