package com.angli.online.service;

import com.angli.online.dto.response.LearningStatsResponse;

public interface LearningStatsService {

    LearningStatsResponse getLearningStats(Long userId);

}