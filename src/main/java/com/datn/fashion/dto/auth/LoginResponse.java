package com.datn.fashion.dto.auth;
import com.datn.fashion.dto.profile.ProfileResponse;
public record LoginResponse(String token,ProfileResponse user) { @Override public String toString() { return "LoginResponse[token=REDACTED]"; } }
