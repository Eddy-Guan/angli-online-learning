package com.angli.online.service.impl;

import com.angli.online.dto.response.LearningStatsResponse;
import com.angli.online.mapper.CheckinMapper;
import com.angli.online.mapper.CourseChapterMapper;
import com.angli.online.mapper.CourseMapper;
import com.angli.online.service.LearningStatsService;
import org.springframework.stereotype.Service;

@Service
public class LearningStatsServiceImpl implements LearningStatsService {

    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final CheckinMapper checkinMapper;

    public LearningStatsServiceImpl(CourseMapper courseMapper, CourseChapterMapper courseChapterMapper, CheckinMapper checkinMapper) {
        this.courseMapper = courseMapper;
        this.courseChapterMapper = courseChapterMapper;
        this.checkinMapper = checkinMapper;
    }

    @Override
    public LearningStatsResponse getLearningStats(Long userId) {
        LearningStatsResponse response = new LearningStatsResponse();
        
        Integer totalHours = courseMapper.sumCompletedCourseHours(userId);
        response.setTotalHours(totalHours != null ? totalHours : 0);
        
        Integer completedLessons = courseChapterMapper.countCompletedChapters(userId);
        response.setCompletedLessons(completedLessons != null ? completedLessons : 0);
        
        Integer streakDays = checkinMapper.getContinuousDays(userId);
        response.setStreakDays(streakDays != null ? streakDays : 0);
        
        return response;
    }

}