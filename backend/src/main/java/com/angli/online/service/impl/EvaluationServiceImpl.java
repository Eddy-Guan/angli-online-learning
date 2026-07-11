package com.angli.online.service.impl;

import com.angli.online.dto.request.EvaluationRequest;
import com.angli.online.entity.Course;
import com.angli.online.entity.CourseChapter;
import com.angli.online.entity.Evaluation;
import com.angli.online.entity.User;
import com.angli.online.mapper.CourseChapterMapper;
import com.angli.online.mapper.CourseMapper;
import com.angli.online.mapper.EvaluationMapper;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.EvaluationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationMapper evaluationMapper;
    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final UserMapper userMapper;

    public EvaluationServiceImpl(EvaluationMapper evaluationMapper, CourseMapper courseMapper,
                                 CourseChapterMapper courseChapterMapper, UserMapper userMapper) {
        this.evaluationMapper = evaluationMapper;
        this.courseMapper = courseMapper;
        this.courseChapterMapper = courseChapterMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Evaluation createEvaluation(Long userId, EvaluationRequest request) {
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Long chapterCount = courseChapterMapper.selectCount(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, request.getCourseId()));
        Long completedCount = courseChapterMapper.selectCount(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, request.getCourseId())
                .eq(CourseChapter::getIsCompleted, 1));

        if (chapterCount > 0 && completedCount < chapterCount) {
            throw new RuntimeException("课程章节尚未全部完成，无法评价");
        }

        Evaluation existing = evaluationMapper.selectOne(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getUserId, userId)
                .eq(Evaluation::getCourseId, request.getCourseId()));
        if (existing != null) {
            throw new RuntimeException("您已评价过该课程");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setUserId(userId);
        evaluation.setCourseId(request.getCourseId());
        evaluation.setStudentId(request.getStudentId());
        evaluation.setRating(request.getRating());
        evaluation.setComment(request.getComment());

        evaluationMapper.insert(evaluation);
        return evaluation;
    }

    @Override
    public Evaluation getEvaluationById(Long id) {
        return evaluationMapper.selectById(id);
    }

    @Override
    public List<Evaluation> getEvaluationsByCourseId(Long courseId) {
        return evaluationMapper.selectList(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getCourseId, courseId));
    }

    @Override
    public Double getCourseAverageRating(Long courseId) {
        List<Evaluation> evaluations = getEvaluationsByCourseId(courseId);
        if (evaluations.isEmpty()) {
            return 0.0;
        }
        int totalRating = evaluations.stream()
                .mapToInt(Evaluation::getRating)
                .sum();
        return Math.round((double) totalRating / evaluations.size() * 10) / 10.0;
    }

}
