package dev.handsup.notification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.handsup.auth.jwt.JwtAuthorization;
import dev.handsup.notification.dto.SaveFCMTokenRequest;
import dev.handsup.notification.service.FCMService;
import dev.handsup.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FCMController {
	private final FCMService fcmService;

	@PostMapping("/api/fcm-tokens")
	@Operation(summary = "FCM 토큰 저장 API", description = "{사용자 아이디 : FCM 토큰}을 redis에 저장")
	@ApiResponse(useReturnTypeSchema = true)
	public ResponseEntity<HttpStatus> saveFCMToken(
		@Parameter(hidden = true) @JwtAuthorization User user,
		@RequestBody @Valid SaveFCMTokenRequest request
	) {
		fcmService.saveFcmToken(user.getId(), request.fcmToken());
		return ResponseEntity.ok(HttpStatus.OK);
	}
}
