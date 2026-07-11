package com.angli.online.service;

import com.angli.online.entity.DailySummary;
import com.angli.online.entity.Enrollment;

import java.util.List;

public interface DailySummaryService {

    DailySummary createDailySummary(Long teacherId, Long courseId, String content, String summaryDate);

    List<DailySummary> getDailySummariesByCourse(Long courseId);

    List<Enrollment> getStudentsByCourse(Long courseId);

}
