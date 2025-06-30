package dev.handsup.notification.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.handsup.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	Slice<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);
}
