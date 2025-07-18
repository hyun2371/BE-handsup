package dev.handsup.notification.service;

import org.springframework.stereotype.Service;

import dev.handsup.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationSender {
	private final NotificationService notificationService;
	private final FcmService fcmService;

	public void sendNotification(Long senderId, Long receiverId, String senderNickname, Long auctionId,
		NotificationType notificationType) {
		String content = notificationType.processContent(senderNickname);
		fcmService.sendNotification(
			receiverId,
			content,
			notificationType
		);
		notificationService.saveNotification(senderId, receiverId, auctionId, content, notificationType);
	}
}
