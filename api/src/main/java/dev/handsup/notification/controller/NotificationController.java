package dev.handsup.notification.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.handsup.auth.jwt.JwtAuthorization;
import dev.handsup.common.dto.PageResponse;
import dev.handsup.notification.dto.NotificationResponse;
import dev.handsup.notification.service.NotificationService;
import dev.handsup.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController {
	private final NotificationService notificationService;

	@GetMapping("/api/notifications")
	@Operation(summary = "알림 전체 조회 API", description = "특정 사용자가 받은 알림을 최근 생성 순으로 전체 조회한다")
	@ApiResponse(useReturnTypeSchema = true)
	public ResponseEntity<PageResponse<NotificationResponse>> getUserNotifications(
		@Parameter(hidden = true) @JwtAuthorization User user,
		Pageable pageable
	) {
		PageResponse<NotificationResponse> response = notificationService.getNotifications(user, pageable);
		return ResponseEntity.ok(response);
	}
}
