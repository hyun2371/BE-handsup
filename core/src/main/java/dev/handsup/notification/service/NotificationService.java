package dev.handsup.notification.service;

import org.springframework.stereotype.Service;

import dev.handsup.notification.domain.Notification;
import dev.handsup.notification.domain.NotificationType;
import dev.handsup.notification.repository.NotificationRepository;
import dev.handsup.user.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository notificationRepository;

	@Transactional
	public void saveNotification(User sender, User receiver, Long auctionId, String content, NotificationType type){
		Notification notification = Notification.of(
			receiver.getId(),
			sender.getId(),
			auctionId,
			type,
			content
		);
		notificationRepository.save(notification);
	}
}
