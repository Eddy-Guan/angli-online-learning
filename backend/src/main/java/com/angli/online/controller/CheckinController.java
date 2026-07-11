package com.angli.online.controller;

import com.angli.online.dto.response.ApiResponse;
import com.angli.online.service.CheckinService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/parent/checkin")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> checkin(@RequestParam Long userId, @RequestParam(required = false) Long studentId) {
        boolean success = checkinService.checkin(userId, studentId);
        int continuousDays = checkinService.getContinuousDays(userId, studentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("continuousDays", continuousDays);
        
        return ApiResponse.success(result);
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getCheckinStatus(@RequestParam Long userId, @RequestParam(required = false) Long studentId) {
        boolean hasChecked = checkinService.hasCheckedToday(userId, studentId);
        int continuousDays = checkinService.getContinuousDays(userId, studentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("hasChecked", hasChecked);
        result.put("continuousDays", continuousDays);
        
        return ApiResponse.success(result);
    }

}