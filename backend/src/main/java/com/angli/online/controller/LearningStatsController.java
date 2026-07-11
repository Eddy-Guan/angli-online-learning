package com.angli.online.controller;

import com.angli.online.dto.response.ApiResponse;
import com.angli.online.dto.response.LearningStatsResponse;
import com.angli.online.service.LearningStatsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parent/stats")
public class LearningStatsController {

    private final LearningStatsService learningStatsService;

    public LearningStatsController(LearningStatsService learningStatsService) {
        this.learningStatsService = learningStatsService;
    }

    @GetMapping("/learning")
    public ApiResponse<LearningStatsResponse> getLearningStats(@RequestParam Long userId) {
        LearningStatsResponse stats = learningStatsService.getLearningStats(userId);
        return ApiResponse.success(stats);
    }

}