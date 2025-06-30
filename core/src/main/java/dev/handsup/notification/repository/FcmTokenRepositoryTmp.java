package dev.handsup.notification.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class FcmTokenRepositoryTmp {

	private static final String PREFIX = "fcmToken:";

	private final StringRedisTemplate redisTemplate;

	public void saveFcmToken(Long userId, String fcmToken) {
		if (!hasKey(userId)) {
			redisTemplate.opsForValue()
				.set(PREFIX + userId, fcmToken, 3000, TimeUnit.DAYS);
		}
	}

	public String getFcmToken(Long userId) {
		return redisTemplate.opsForValue().get(PREFIX + userId);
	}

	public void deleteFcmToken(Long userId) {
		redisTemplate.delete(PREFIX + userId);
	}

	public boolean hasKey(Long userId) {
		return redisTemplate.hasKey(PREFIX + userId);
	}
}