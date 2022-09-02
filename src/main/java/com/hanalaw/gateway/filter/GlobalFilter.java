package com.hanalaw.gateway.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.hanalaw.gateway.exception.ThrottleException;
import com.hanalaw.gateway.limiter.TokenBucketLimiter;

import lombok.Data;
import reactor.core.publisher.Mono;

@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

	private static final Logger logger = LogManager.getLogger(GlobalFilter.class);

	public GlobalFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return ((exchange, chain) -> {
			try {
				TokenBucketLimiter.decrease(); // 버켓 decrease에 성공하는 경우 리퀘스트는 포워딩된다.
				logger.info("Forwarding the request..");
				logger.info("GlobalFilter baseMessage>>>>>>" + config.getBaseMessage());
				if (config.isPreLogger()) {
					logger.info("GlobalFilter Start>>>>>>" + exchange.getRequest());
				}
				
			} catch (ThrottleException e) {
				logger.info("Request Throttled.");
				return onError(exchange, "Too Many Request", HttpStatus.TOO_MANY_REQUESTS);
			}  
			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				if (config.isPostLogger()) {
					logger.info("GlobalFilter End>>>>>>" + exchange.getResponse());
				}
			}));
		});
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);

		logger.error(err);
		return response.setComplete();
	}
	
	@Data
	public static class Config {
		private String baseMessage;
		private boolean preLogger;
		private boolean postLogger;
	}
}