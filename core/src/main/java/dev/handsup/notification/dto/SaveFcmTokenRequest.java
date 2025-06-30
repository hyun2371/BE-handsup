package dev.handsup.notification.dto;

import static lombok.AccessLevel.*;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder(access = PRIVATE)
public record SaveFcmTokenRequest(
	@NotEmpty(message = "fcmToken을 입력해주세요.")
	String fcmToken
) {
	public static SaveFcmTokenRequest from(String fcmToken) {
		return SaveFcmTokenRequest.builder()
			.fcmToken(fcmToken)
			.build();
	}
}