package com.datn.fashion;

import com.datn.fashion.entity.User;
import com.datn.fashion.repository.UserRepository;
import com.datn.fashion.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Actual HTTP/security/catalog; isolated H2 account, no mocked beans or responses. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
    "spring.datasource.url=jdbc:h2:mem:beauty_real_http;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "debug=false", "logging.level.root=WARN"})
@ActiveProfiles("test")
class BeautyRealHttpTest {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired JwtService jwt;
    @Autowired ObjectMapper mapper;
    final HttpClient client=HttpClient.newHttpClient();
    HttpResponse<String> get(String path,String token) throws Exception {
        var req=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).timeout(Duration.ofSeconds(10)).GET();
        if(token!=null)req.header("Authorization","Bearer "+token);
        return client.send(req.build(),HttpResponse.BodyHandlers.ofString());
    }
    @Test void realCatalogAllShapesTypesAndDetails() throws Exception {
        var u=new User();u.email="beauty-http-"+UUID.randomUUID()+"@example.com";u.passwordHash="test-only";
        u=users.saveAndFlush(u);
        try {
            String token=jwt.create(u.id),base="/api/beauty/recommendations";
            assertEquals(401,get(base+"?faceShape=OVAL",null).statusCode());
            assertEquals(401,get(base+"?faceShape=OVAL","invalid").statusCode());
            var results=new ArrayList<Map<String,Object>>();
            StringBuilder counts=new StringBuilder("faceShape,type,httpStatus,itemCount,detailsChecked\n");
            for(String shape:List.of("HEART","OBLONG","OVAL","ROUND","SQUARE")) {
                for(String type:List.of("HAIRSTYLE","MAKEUP")) {
                    String path=base+"?faceShape="+shape+"&type="+type;
                    var res=get(path,token);assertEquals(200,res.statusCode());
                    var data=mapper.readTree(res.body());
                    assertEquals(shape,data.path("confirmedFaceShape").asText());
                    assertEquals("FACE_SHAPE_RULE_BASED",data.path("mechanism").asText());
                    var items=data.path("items");assertTrue(items.size()>=2);
                    int checked=0;
                    for(var item:items) {
                        assertEquals(type,item.path("type").asText());
                        assertTrue(item.path("matchesConfirmedShape").asBoolean());
                        assertFalse(item.path("reason").asText().isBlank());
                        var detail=get(base+"/"+item.path("id").asText()+"?faceShape="+shape,token);
                        assertEquals(200,detail.statusCode());
                        var d=mapper.readTree(detail.body()).path("item");
                        assertEquals(item.path("id").asText(),d.path("id").asText());
                        assertTrue(d.path("steps").size()>0);checked++;
                    }
                    counts.append(shape+","+type+",200,"+items.size()+","+checked+"\n");
                    results.add(Map.of("request",path,"status",res.statusCode(),"response",data));
                }
                var all=get(base+"?faceShape="+shape+"&scope=ALL",token);
                assertEquals(200,all.statusCode());assertEquals(12,mapper.readTree(all.body()).path("items").size());
            }
            assertEquals(400,get(base+"?faceShape=UNKNOWN",token).statusCode());
            assertEquals(404,get(base+"/missing?faceShape=OVAL",token).statusCode());
            var output=Path.of(System.getenv().getOrDefault("NOVA_BEAUTY_TEST_OUTPUT","D:/DATN/FE/docs/task_11_1"));
            Files.writeString(output.resolve("API_COUNTS.csv"),counts);
            mapper.writerWithDefaultPrettyPrinter().writeValue(output.resolve("API_RESPONSES.json").toFile(),results);
        } finally {users.deleteById(u.id);}
    }
}
