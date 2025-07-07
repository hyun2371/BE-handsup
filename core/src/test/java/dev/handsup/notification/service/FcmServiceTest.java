package dev.handsup.notification.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;

import dev.handsup.fixture.UserFixture;
import dev.handsup.notification.domain.NotificationType;
import dev.handsup.notification.repository.FcmTokenRepository;
import dev.handsup.user.domain.User;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {
	private final User receiver = UserFixture.user1();
	@Mock
	private FcmTokenRepository fcmTokenRepository;
	@Mock
	private FirebaseMessaging firebaseMessaging;
	@InjectMocks
	private FcmService fcmService;

	@Test
	@DisplayName("메시지를 성공적으로 보낸다]")
	void sendMessageSuccessTest() throws FirebaseMessagingException {
		// given
		String fcmToken = "fcmToken123";
		given(fcmTokenRepository.getFcmToken(receiver.getId())).willReturn(fcmToken);

		// when
		fcmService.sendNotification(
			receiver.getId(),
			"ㅎㅎ",
			NotificationType.BIDDING_CREATED
		);

		// then
		verify(firebaseMessaging, times(1)).send(any());
	}
}