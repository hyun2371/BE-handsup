package dev.handsup.notification.service;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import dev.handsup.common.exception.ValidationException;
import dev.handsup.notification.domain.NotificationType;
import dev.handsup.notification.repository.FcmTokenRepositoryTmp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class FcmServiceTmp {
	private final FcmTokenRepositoryTmp fcmTokenRepository;
	private final FirebaseMessaging firebaseMessaging;

	public void sendNotification(
		Long receiverId,
		String content,
		NotificationType notificationType
	) {
		String fcmToken = fcmTokenRepository.getFcmToken(receiverId);
		if (fcmToken == null) {
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
			log.info("Sent message: {}, to: {}", message, receiverId);
		} catch (FirebaseMessagingException e) {
			throw new ValidationException(e.getMessage());
		}
	}

	public void saveFcmToken(Long userId, String fcmToken) {
		fcmTokenRepository.saveFcmToken(userId, fcmToken);
	}

	public void deleteFcmToken(Long userId) {
		fcmTokenRepository.deleteFcmToken(userId);
	}
}
