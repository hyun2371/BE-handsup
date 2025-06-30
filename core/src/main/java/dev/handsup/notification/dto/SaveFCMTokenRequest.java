package dev.handsup.notification.dto;

import static lombok.AccessLevel.*;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder(access = PRIVATE)
public record SaveFCMTokenRequest(
	@NotEmpty(message = "fcmToken을 입력해주세요.")
	String fcmToken
) {
	public static SaveFCMTokenRequest from(String fcmToken) {
		return SaveFCMTokenRequest.builder()
			.fcmToken(fcmToken)
			.build();
	}
}