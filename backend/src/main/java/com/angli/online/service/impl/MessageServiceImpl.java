package com.angli.online.service.impl;

import com.angli.online.entity.Message;
import com.angli.online.entity.Notification;
import com.angli.online.entity.User;
import com.angli.online.mapper.MessageMapper;
import com.angli.online.mapper.NotificationMapper;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.MessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

  private final MessageMapper messageMapper;
  private final NotificationMapper notificationMapper;
  private final UserMapper userMapper;

  public MessageServiceImpl(MessageMapper messageMapper, NotificationMapper notificationMapper,
      UserMapper userMapper) {
    this.messageMapper = messageMapper;
    this.notificationMapper = notificationMapper;
    this.userMapper = userMapper;
  }

  @Override
  public int getUnreadCount(Long userId) {
    Long count = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
        .eq(Message::getUserId, userId)
        .eq(Message::getIsRead, 0));
    return count.intValue();
  }

  @Override
  public List<Message> getMessages(Long userId, int offset, int limit) {
    return messageMapper.selectList(new LambdaQueryWrapper<Message>()
        .eq(Message::getUserId, userId)
        .orderByDesc(Message::getCreatedAt)
        .last("LIMIT " + offset + ", " + limit));
  }

  @Override
  public void markAsRead(Long userId, Long messageId) {
    Message message = messageMapper.selectById(messageId);
    if (message != null && message.getUserId().equals(userId) && message.getIsRead() == 0) {
      message.setIsRead(1);
      message.setReadAt(LocalDateTime.now());
      messageMapper.updateById(message);
    }
  }

  @Override
  public void markAllAsRead(Long userId) {
    List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
        .eq(Message::getUserId, userId)
        .eq(Message::getIsRead, 0));
    for (Message message : messages) {
      message.setIsRead(1);
      message.setReadAt(LocalDateTime.now());
      messageMapper.updateById(message);
    }
  }

  @Override
  public Notification createNotification(String title, String content, String targetRole) {
    Notification notification = new Notification();
    notification.setTitle(title);
    notification.setContent(content);
    notification.setTargetRole(targetRole);
    notification.setIsPublished(0);

    notificationMapper.insert(notification);
    return notification;
  }

  @Override
  public List<Notification> getAllNotifications() {
    return notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
        .orderByDesc(Notification::getCreatedAt));
  }

  @Override
  public void publishNotification(Long id) {
    Notification notification = notificationMapper.selectById(id);
    if (notification == null) {
      throw new RuntimeException("公告不存在");
    }

    notification.setIsPublished(1);
    notification.setPublishedAt(LocalDateTime.now());
    notificationMapper.updateById(notification);

    List<User> targetUsers = getTargetUsers(notification.getTargetRole());

    for (User user : targetUsers) {
      Message message = new Message();
      message.setUserId(user.getId());
      message.setType("NOTIFICATION");
      message.setTitle(notification.getTitle());
      message.setContent(notification.getContent());
      message.setRelatedId(id);
      message.setIsRead(0);
      messageMapper.insert(message);
    }
  }

  public void publishNotification(Notification notification) {
    notificationMapper.insert(notification);

    List<User> targetUsers = getTargetUsers(notification.getTargetRole());

    for (User user : targetUsers) {
      Message message = new Message();
      message.setUserId(user.getId());
      message.setType("NOTIFICATION");
      message.setTitle(notification.getTitle());
      message.setContent(notification.getContent());
      message.setRelatedId(notification.getId());
      message.setIsRead(0);
      messageMapper.insert(message);
    }
  }

  private List<User> getTargetUsers(String targetRole) {
    if ("ALL".equals(targetRole)) {
      return userMapper.selectList(new LambdaQueryWrapper<User>()
          .eq(User::getStatus, "ACTIVE"));
    } else {
      return userMapper.selectList(new LambdaQueryWrapper<User>()
          .eq(User::getStatus, "ACTIVE")
          .eq(User::getRole, targetRole));
    }
  }

}
