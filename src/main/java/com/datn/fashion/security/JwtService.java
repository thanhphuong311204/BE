package com.datn.fashion.security;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
@Service
public class JwtService {
 private final JwtEncoder encoder;
 private final String issuer;
 private final long ttl;
 public JwtService(JwtEncoder encoder,@Value("${app.jwt.issuer}") String issuer,@Value("${app.jwt.ttl-seconds}") long ttl) {
  this.encoder=encoder;this.issuer=issuer;this.ttl=ttl;
  if(ttl<60 || ttl>86400) throw new IllegalArgumentException("JWT TTL must be between 60 and 86400 seconds.");
 }
 public String create(Long userId) {
  Instant now=Instant.now();
  var claims=JwtClaimsSet.builder().issuer(issuer).subject(userId.toString()).issuedAt(now).expiresAt(now.plusSeconds(ttl)).build();
  return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
 }
}
