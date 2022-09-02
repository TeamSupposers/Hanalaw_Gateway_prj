package com.hanalaw.gateway.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import lombok.Data;
import reactor.core.publisher.Mono;

@Component
public class MemberFilter extends AbstractGatewayFilterFactory<MemberFilter.Config> {

	private static final Logger logger = LogManager.getLogger(MemberFilter.class);

	public MemberFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return ((exchange, chain) -> {
			logger.info("MemberFilter baseMessage>>>>>>" + config.getBaseMessage());
			if (config.isPreLogger()) {
				logger.info("MemberFilter Start>>>>>>" + exchange.getRequest());
			}
			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				if (config.isPostLogger()) {
					logger.info("MemberFilter End>>>>>>" + exchange.getResponse());
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