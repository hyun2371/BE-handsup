package dev.handsup.common.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FcmConfig {

	@Value("${fcm.key}")
	private String FIREBASE_KEY_PATH;

	@PostConstruct
	public void initialize() {
		if (!FirebaseApp.getApps().isEmpty())
			return;
		try {
			GoogleCredentials googleCredentials = GoogleCredentials
				.fromStream(new ClassPathResource(FIREBASE_KEY_PATH).getInputStream());
			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(googleCredentials)
				.build();
			FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	@Bean
	public FirebaseMessaging firebaseMessaging() {
		return FirebaseMessaging.getInstance();
	}
}
