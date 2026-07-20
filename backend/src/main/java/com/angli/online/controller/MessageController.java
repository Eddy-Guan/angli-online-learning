package com.angli.online.controller;

import com.angli.online.dto.response.ApiResponse;
import com.angli.online.entity.Message;
import com.angli.online.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getMessages(@RequestParam Long userId,
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "20") int limit) {

        List<Message> messages = messageService.getMessages(userId, offset, limit);
        int unreadCount = messageService.getUnreadCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", messages);
        result.put("unreadCount", unreadCount);

        return ApiResponse.success(result);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Object>> getUnreadCount(@RequestParam Long userId) {
        int count = messageService.getUnreadCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("count", count);

        return ApiResponse.success(result);
    }

    @PutMapping("/read/{messageId}")
    public ApiResponse<Void> markAsRead(@RequestParam Long userId, @PathVariable Long messageId) {
        messageService.markAsRead(userId, messageId);
        return ApiResponse.success("已标记为已读");
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@RequestParam Long userId) {
        messageService.markAllAsRead(userId);
        return ApiResponse.success("已全部标记为已读");
    }

}