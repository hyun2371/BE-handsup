package dev.handsup.notification.service;

import org.springframework.stereotype.Service;

import dev.handsup.notification.domain.NotificationType;
import dev.handsup.user.domain.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationSender {
	private final NotificationService notificationService;
	private final FCMService fcmService;

	public void sendNotification(User sender, User receiver, Long auctionId, NotificationType notificationType) {
		String content = notificationType.processContent(sender.getNickname());
		fcmService.sendNotification(
			receiver.getId(),
			content,
			notificationType
		);
		notificationService.saveNotification(sender, receiver, auctionId, content, notificationType);
	}
}
