package com.angli.online.controller;

import com.angli.online.dto.response.ApiResponse;
import com.angli.online.dto.response.CourseResponse;
import com.angli.online.entity.CourseChapter;
import com.angli.online.entity.Evaluation;
import com.angli.online.mapper.CourseChapterMapper;
import com.angli.online.mapper.EvaluationMapper;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.CourseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/courses")
public class CourseController {

    private final CourseService courseService;
    private final CourseChapterMapper courseChapterMapper;
    private final EvaluationMapper evaluationMapper;
    private final UserMapper userMapper;

    public CourseController(CourseService courseService, CourseChapterMapper courseChapterMapper,
                           EvaluationMapper evaluationMapper, UserMapper userMapper) {
        this.courseService = courseService;
        this.courseChapterMapper = courseChapterMapper;
        this.evaluationMapper = evaluationMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/recommended")
    public ApiResponse<List<CourseResponse>> getRecommendedCourses() {
        List<CourseResponse> courses = courseService.getRecommendedCourses();
        return ApiResponse.success(courses);
    }

    @GetMapping("/hot")
    public ApiResponse<List<CourseResponse>> getHotCourses() {
        List<CourseResponse> courses = courseService.getHotCourses();
        return ApiResponse.success(courses);
    }

    @GetMapping("/all")
    public ApiResponse<List<CourseResponse>> getAllCourses() {
        List<CourseResponse> courses = courseService.getAllPublishedCourses();
        return ApiResponse.success(courses);
    }

    @GetMapping("/search")
    public ApiResponse<List<CourseResponse>> searchCourses(@RequestParam String keyword) {
        List<CourseResponse> courses = courseService.searchCourses(keyword);
        return ApiResponse.success(courses);
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getCourseById(@PathVariable Long id) {
        CourseResponse course = courseService.getCourseById(id);
        return ApiResponse.success(course);
    }

    @GetMapping("/{id}/chapters")
    public ApiResponse<List<CourseChapter>> getCourseChapters(@PathVariable Long id) {
        List<CourseChapter> chapters = courseChapterMapper.selectList(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, id)
                .orderByAsc(CourseChapter::getChapterOrder));
        return ApiResponse.success(chapters);
    }

    @GetMapping("/{id}/evaluations")
    public ApiResponse<List<Evaluation>> getCourseEvaluations(@PathVariable Long id) {
        List<Evaluation> evaluations = evaluationMapper.selectList(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getCourseId, id)
                .orderByDesc(Evaluation::getCreatedAt));
        return ApiResponse.success(evaluations);
    }

}