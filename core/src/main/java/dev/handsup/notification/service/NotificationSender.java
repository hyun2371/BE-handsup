package dev.handsup.notification.service;

import org.springframework.stereotype.Service;

import dev.handsup.auction.domain.Auction;
import dev.handsup.notification.domain.NotificationType;
import dev.handsup.user.domain.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationSender {
	private  final FCMService fcmService;

	public void sendNotification(User sender, User receiver, Auction auction, NotificationType type) {
		fcmService.sendNotification(
			sender.getId(),
			sender.getNickname(),
			receiver.getId(),
			auction.getId(),
			type
		);
	}
}
