package com.angli.online.service.impl;

import com.angli.online.entity.Course;
import com.angli.online.entity.DailySummary;
import com.angli.online.entity.Enrollment;
import com.angli.online.entity.User;
import com.angli.online.mapper.CourseMapper;
import com.angli.online.mapper.DailySummaryMapper;
import com.angli.online.mapper.EnrollmentMapper;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.DailySummaryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DailySummaryServiceImpl implements DailySummaryService {

    private final DailySummaryMapper dailySummaryMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final EnrollmentMapper enrollmentMapper;

    public DailySummaryServiceImpl(DailySummaryMapper dailySummaryMapper, CourseMapper courseMapper,
                                   UserMapper userMapper, EnrollmentMapper enrollmentMapper) {
        this.dailySummaryMapper = dailySummaryMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.enrollmentMapper = enrollmentMapper;
    }

    @Override
    public DailySummary createDailySummary(Long teacherId, Long courseId, String content, String summaryDate) {
        User teacher = userMapper.selectById(teacherId);
        if (teacher == null) {
            throw new RuntimeException("教师不存在");
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        if (!course.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("无权操作该课程");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(summaryDate, formatter);

        DailySummary existing = dailySummaryMapper.selectOne(new LambdaQueryWrapper<DailySummary>()
                .eq(DailySummary::getCourseId, courseId)
                .eq(DailySummary::getSummaryDate, date));
        if (existing != null) {
            throw new RuntimeException("今日总结已发布");
        }

        DailySummary summary = new DailySummary();
        summary.setTeacherId(teacherId);
        summary.setCourseId(courseId);
        summary.setContent(content);
        summary.setSummaryDate(date);

        dailySummaryMapper.insert(summary);
        return summary;
    }

    @Override
    public List<DailySummary> getDailySummariesByCourse(Long courseId) {
        return dailySummaryMapper.selectList(new LambdaQueryWrapper<DailySummary>()
                .eq(DailySummary::getCourseId, courseId)
                .orderByDesc(DailySummary::getSummaryDate));
    }

    @Override
    public List<Enrollment> getStudentsByCourse(Long courseId) {
        return enrollmentMapper.selectList(new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getCourseId, courseId));
    }

}
