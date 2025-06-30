package dev.handsup.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.handsup.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
