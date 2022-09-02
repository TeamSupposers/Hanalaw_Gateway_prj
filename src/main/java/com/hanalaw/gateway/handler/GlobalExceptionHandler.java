package com.hanalaw.gateway.handler;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanalaw.gateway.exception.ThrottleException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-1)
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
	
	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		ServerHttpResponse response = exchange.getResponse();

		if (response.isCommitted()) {
			return Mono.error(ex);
		}

		System.out.println(ex instanceof ThrottleException);
		if(ex instanceof ThrottleException) {
		    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS); // Too many requests
		}
		
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		if (ex instanceof ResponseStatusException) {
			response.setStatusCode(((ResponseStatusException) ex).getStatus());
		}

		Map<String, String> errorMap = new HashMap<>();
		String statusCode = Objects.requireNonNull(response.getStatusCode()).toString();
		if (statusCode.split(" ").length == 2) {
			errorMap.put("ErrorCode", response.getStatusCode().toString().split(" ")[0]);
			errorMap.put("ErrorMsg", response.getStatusCode().toString().split(" ")[1]);
		}

		String error = "Gateway Error";
		try {
			error = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorMap);
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException : " + e.getMessage());
		}

		byte[] bytes = error.getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
		return exchange.getResponse().writeWith(Flux.just(buffer));
	}

}