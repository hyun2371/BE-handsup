package dev.handsup.bidding.service;

import dev.handsup.notification.domain.NotificationType;

public record NotificationEvent(
	Long senderId,
	Long receiverId,
	String receiverNickname,
	Long auctionId,
	NotificationType type
) {
}
