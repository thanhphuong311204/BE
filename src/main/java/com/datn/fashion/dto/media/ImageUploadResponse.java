package com.datn.fashion.dto.media;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageUploadResponse(@JsonProperty("secure_url") String secureUrl,
                                  @JsonProperty("public_id") String publicId) {}
