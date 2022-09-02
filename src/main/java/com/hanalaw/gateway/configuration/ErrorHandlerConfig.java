package com.hanalaw.gateway.configuration;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanalaw.gateway.handler.GlobalExceptionHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ErrorHandlerConfig {

	private final ObjectMapper objectMapper;
		
	@Bean
    public ErrorWebExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler(objectMapper);
    }
	
}
