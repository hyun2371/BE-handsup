package dev.handsup.notification.dto;

public record NotificationResponse(
	Long notificationId,
	Long receiverId,
	Long senderId,
	Long auctionId,
	String type,
	String content,
	String createdAt
) {
}