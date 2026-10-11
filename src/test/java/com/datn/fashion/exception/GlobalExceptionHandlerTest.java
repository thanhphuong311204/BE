package com.datn.fashion.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {
    @RestController
    static class MissingResourceController {
        @GetMapping("/missing-resource")
        void missing() throws NoResourceFoundException {
            throw new NoResourceFoundException(HttpMethod.GET, "missing-resource");
        }
    }

    @Test void missingResourceReturns404InsteadOfServerError() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new MissingResourceController())
            .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/missing-resource"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOT_FOUND"))
            .andExpect(jsonPath("$.timestamp").exists());
    }
}
