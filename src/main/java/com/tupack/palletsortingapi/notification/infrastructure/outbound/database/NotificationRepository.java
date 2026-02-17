package com.tupack.palletsortingapi.notification.infrastructure.outbound.database;

import com.tupack.palletsortingapi.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
           "AND (:unreadOnly = false OR n.isRead = false) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdWithFilter(
        @Param("userId") String userId,
        @Param("unreadOnly") boolean unreadOnly,
        Pageable pageable
    );

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") String userId);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId")
    Page<Notification> findByUserId(@Param("userId") String userId, Pageable pageable);

    void deleteAllByUserId(String userId);
}
