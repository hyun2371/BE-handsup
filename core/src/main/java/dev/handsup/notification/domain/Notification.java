package dev.handsup.notification.domain;

import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import dev.handsup.common.entity.TimeBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class Notification extends TimeBaseEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "notification_id")
	private Long id;

	@Column(name = "receiver_id")
	private Long receiverId;

	@Column(name = "sender_id")
	private Long senderId;

	@Column(name = "auction_id")
	private Long auctionId;

	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private NotificationType type;

	@Column(name = "content")
	private String content;

	@Column(name = "is_read")
	private boolean isRead = false;

	@Builder(access = AccessLevel.PRIVATE)
	private Notification(Long receiverId, Long senderId, Long auctionId, NotificationType type, String content) {
		this.receiverId = receiverId;
		this.senderId = senderId;
		this.auctionId = auctionId;
		this.type = type;
		this.content = content;
	}

	public static Notification of(
		Long receiverId,
		Long senderId,
		Long auctionId,
		String content,
		NotificationType type
	) {
		return Notification.builder()
			.receiverId(receiverId)
			.senderId(senderId)
			.auctionId(auctionId)
			.content(content)
			.type(type)
			.build();
	}
}
