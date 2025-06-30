package dev.handsup.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

	CHAT("채팅 알림", "님이 회원님과의 채팅방에서 속삭이고 있어요."),
	COMMENT("경매 댓글 알림", "님이 회원님의 경매 물품에서 얘기하고 있어요."),
	BOOKMARK("경매 북마크 알림", "님이 회원님의 경매 물품을 관심있어 해요."),
	BIDDING_CREATED("입찰 알림", "님이 회원님 경매에 입찰하였습니다."),
	PURCHASE_WINNING("낙찰 알림", "입찰하신 물품이 낙찰되었습니다."),
	CANCELED_PURCHASE_TRADING("거래 취소 알림", "거래가 취소되었습니다."),
	COMPLETED_PURCHASE_TRADING("거래 완료 알림", "거래가 완료되었습니다.");

	private final String title;
	private final String content;

	public String processContent(String senderNickname) {
		if (this == PURCHASE_WINNING || this == CANCELED_PURCHASE_TRADING) {
			return this.content;
		}
		return senderNickname + this.content;
	}
}
