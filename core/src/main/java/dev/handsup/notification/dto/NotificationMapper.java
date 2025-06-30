package dev.handsup.notification.dto;

import dev.handsup.notification.domain.Notification;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationMapper {

	public static NotificationResponse toNotificationResponse(
		Notification notification
	) {
		return new NotificationResponse(
			notification.getId(),
			notification.getReceiverId(),
			notification.getSenderId(),
			notification.getAuctionId(),
			notification.getType().getTitle(),
			notification.getContent(),
			notification.getCreatedAt().toString()
		);
	}
}
