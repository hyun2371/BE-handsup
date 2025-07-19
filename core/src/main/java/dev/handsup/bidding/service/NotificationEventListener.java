package dev.handsup.bidding.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import dev.handsup.notification.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
	private final NotificationSender notificationSender;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleBiddingEventCompleted(NotificationEvent event) {
		notificationSender.sendNotification(
			event.senderId(),
			event.receiverId(),
			event.receiverNickname(),
			event.auctionId(),
			event.type()
		);
	}
}