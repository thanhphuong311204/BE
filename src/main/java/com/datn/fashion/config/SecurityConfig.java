package com.datn.fashion.config;
import com.datn.fashion.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.web.cors.*;
import javax.crypto.spec.SecretKeySpec;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
 @Bean SecretKeySpec jwtKey(@Value("${app.jwt.secret}") String secret) {
  byte[] bytes=secret.getBytes(StandardCharsets.UTF_8);
  if(bytes.length<32) throw new IllegalArgumentException("JWT_SECRET must contain at least 32 UTF-8 bytes.");
  return new SecretKeySpec(bytes,"HmacSHA256");
 }
 @Bean JwtEncoder jwtEncoder(SecretKeySpec key) { return new NimbusJwtEncoder(new ImmutableSecret<>(key)); }
 @Bean JwtDecoder jwtDecoder(SecretKeySpec key,@Value("${app.jwt.issuer}") String issuer) {
  var decoder=NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
  decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
  return decoder;
 }
 @Bean SecurityFilterChain security(HttpSecurity http,ObjectMapper mapper) throws Exception {
  return http.csrf(c -> c.disable()).cors(c -> {})
   .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a -> a.requestMatchers(org.springframework.http.HttpMethod.POST,"/api/auth/register","/api/auth/login").permitAll().anyRequest().authenticated())
   .oauth2ResourceServer(o -> o.jwt(j -> {}).authenticationEntryPoint((req,res,e) -> {
    res.setStatus(401);res.setContentType("application/json");res.setCharacterEncoding("UTF-8");
    mapper.writeValue(res.getOutputStream(),new ApiError("UNAUTHORIZED","Phiên đăng nhập hết hạn hoặc không hợp lệ.",Map.of()));
   }))
   .exceptionHandling(e -> e.authenticationEntryPoint((req,res,ex) -> {
    res.setStatus(401);res.setContentType("application/json");res.setCharacterEncoding("UTF-8");
    mapper.writeValue(res.getOutputStream(),new ApiError("UNAUTHORIZED","Vui lòng đăng nhập.",Map.of()));
   }).accessDeniedHandler((req,res,ex) -> {
    res.setStatus(403);res.setContentType("application/json");res.setCharacterEncoding("UTF-8");
    mapper.writeValue(res.getOutputStream(),new ApiError("FORBIDDEN","Bạn không có quyền truy cập.",Map.of()));
   })).build();
 }
 @Bean CorsConfigurationSource cors(@Value("${app.cors-origins}") String origins) {
  var c=new CorsConfiguration(); c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
  c.setAllowedMethods(List.of("GET","POST","PUT","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type"));c.setMaxAge(3600L);
  var source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",c);return source;
 }
}
