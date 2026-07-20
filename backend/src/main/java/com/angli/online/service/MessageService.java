package com.angli.online.service;

import com.angli.online.entity.Message;
import com.angli.online.entity.Notification;

import java.util.List;

public interface MessageService {

  int getUnreadCount(Long userId);

  List<Message> getMessages(Long userId, int offset, int limit);

  void markAsRead(Long userId, Long messageId);

  void markAllAsRead(Long userId);

  Notification createNotification(String title, String content, String targetRole);

  List<Notification> getAllNotifications();

  void publishNotification(Long id);

  void publishNotification(Notification notification);

}
