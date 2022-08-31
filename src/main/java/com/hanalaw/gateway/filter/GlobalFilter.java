package com.hanalaw.gateway.filter;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import com.hanalaw.gateway.exception.ThrottleException;
import com.hanalaw.gateway.limiter.TokenBucketLimiter;

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
			} catch (ThrottleException e) {
				logger.info("Request Throttled.");
			}
			logger.info("GlobalFilter baseMessage>>>>>>" + config.getBaseMessage());
			if (config.isPreLogger()) {
				logger.info("GlobalFilter Start>>>>>>" + exchange.getRequest());
			}
			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				if (config.isPostLogger()) {
					logger.info("GlobalFilter End>>>>>>" + exchange.getResponse());
				}
			}));
		});
	}

	@Data
	public static class Config {
		private String baseMessage;
		private boolean preLogger;
		private boolean postLogger;
	}
}