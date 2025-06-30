package dev.handsup.notification.dto;

import static lombok.AccessLevel.*;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder(access = PRIVATE)
public record SaveFcmTokenRequestTmp(
	@NotEmpty(message = "fcmToken을 입력해주세요.")
	String fcmToken
) {
	public static SaveFcmTokenRequestTmp from(String fcmToken) {
		return SaveFcmTokenRequestTmp.builder()
			.fcmToken(fcmToken)
			.build();
	}
}