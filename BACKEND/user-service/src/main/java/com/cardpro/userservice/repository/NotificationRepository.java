package com.cardpro.userservice.repository;

import com.cardpro.userservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUserIdOrderByTimestampDesc(String userId);
}