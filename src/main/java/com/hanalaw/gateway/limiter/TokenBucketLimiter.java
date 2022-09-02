package com.hanalaw.gateway.limiter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import com.hanalaw.gateway.exception.ThrottleException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenBucketLimiter {

	private static final Integer MAX_BUCKET_SIZE_PER_MIN = 50;
	private static Integer bucket = MAX_BUCKET_SIZE_PER_MIN; // 최초 버켓 100으로 시작

	static {
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				increase();
			}
		}, 1 * 60 * 1000, 1 * 60 * 1000); // 1분에 100개 리필, 처음 타이머 시작 후 60초 이후에 타이머 시작.
	}

	public synchronized static void increase() {
		bucket = MAX_BUCKET_SIZE_PER_MIN; // 버켓 리필
		log.info("Reset the bucket size to {}", bucket);
	}

	public synchronized static void decrease() throws ThrottleException {
		if (bucket > 0) {
			bucket--;
			log.info("decreased the bucket size to {}", bucket);
			return;
		}

		LocalTime time = LocalTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

		throw new ThrottleException("Request Throttled " + time.format(formatter));
	}

}