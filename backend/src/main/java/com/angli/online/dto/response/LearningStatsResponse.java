package com.angli.online.dto.response;

import lombok.Data;

@Data
public class LearningStatsResponse {

    private Integer totalHours;

    private Integer completedLessons;

    private Integer streakDays;

}