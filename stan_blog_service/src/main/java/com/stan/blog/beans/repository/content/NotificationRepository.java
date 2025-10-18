package com.stan.blog.beans.repository.content;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.content.NotificationEntity;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    Page<NotificationEntity> findByRecipientIdAndDeletedFalse(Long recipientId, Pageable pageable);
    Page<NotificationEntity> findByRecipientIdAndDeletedFalseAndIsRead(Long recipientId, Boolean isRead, Pageable pageable);
    List<NotificationEntity> findByRecipientIdAndDeletedFalseAndIsReadFalse(Long recipientId);
    long countByRecipientIdAndDeletedFalseAndIsReadFalse(Long recipientId);
}