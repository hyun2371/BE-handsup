package dev.handsup.notification.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.handsup.common.dto.CommonMapper;
import dev.handsup.common.dto.PageResponse;
import dev.handsup.notification.domain.Notification;
import dev.handsup.notification.domain.NotificationType;
import dev.handsup.notification.dto.NotificationMapper;
import dev.handsup.notification.dto.NotificationResponse;
import dev.handsup.notification.repository.NotificationRepository;
import dev.handsup.user.domain.User;
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
			content,
			type
		);
		notificationRepository.save(notification);
	}

	@Transactional(readOnly = true)
	public PageResponse<NotificationResponse> getNotifications(User user, Pageable pageable) {
		Slice<NotificationResponse> notificationResponsePage = notificationRepository
			.findByReceiverIdOrderByCreatedAtDesc(user.getId(), pageable)
			.map(NotificationMapper::toNotificationResponse);

		return CommonMapper.toPageResponse(notificationResponsePage);
	}
}
