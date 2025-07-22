package dev.handsup.notification.service;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import dev.handsup.notification.domain.NotificationType;
import dev.handsup.notification.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class FcmService {
	private final FcmTokenRepository fcmTokenRepository;
	private final FirebaseMessaging firebaseMessaging;

	public void sendNotification(
		Long receiverId,
		String content,
		NotificationType notificationType
	) {
		String fcmToken = fcmTokenRepository.getFcmToken(receiverId);
		if (fcmToken == null) {
			log.info("알림/fcm토큰 없음");
			return;
		}

		Message message = Message.builder()
			.setNotification(Notification.builder()
				.setTitle(notificationType.getTitle())
				.setBody(content)
				.build())
			.setToken(fcmToken)
			.build();

		send(message, receiverId);
	}

	private void send(Message message, Long receiverId) {
		try {
			firebaseMessaging.send(message);
			log.info("알림 전송 성공: {} to: {}", message, receiverId);
		} catch (FirebaseMessagingException e) {
			log.error("알림 발송 실패 receiverId={} message={}",
			receiverId,e.getMessage());
		}
	}

	public void saveFcmToken(Long userId, String fcmToken) {
		fcmTokenRepository.saveFcmToken(userId, fcmToken);
	}

	public void deleteFcmToken(Long userId) {
		fcmTokenRepository.deleteFcmToken(userId);
	}
}
