package com.angli.online.controller;

import com.angli.online.dto.request.CourseCreateRequest;
import com.angli.online.dto.response.ApiResponse;
import com.angli.online.entity.Course;
import com.angli.online.entity.DailySummary;
import com.angli.online.entity.Enrollment;
import com.angli.online.entity.Message;
import com.angli.online.service.TeacherCourseService;
import com.angli.online.service.DailySummaryService;
import com.angli.online.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherCourseService teacherCourseService;
    private final DailySummaryService dailySummaryService;
    private final MessageService messageService;

    public TeacherController(TeacherCourseService teacherCourseService, 
                            DailySummaryService dailySummaryService,
                            MessageService messageService) {
        this.teacherCourseService = teacherCourseService;
        this.dailySummaryService = dailySummaryService;
        this.messageService = messageService;
    }

    @PostMapping("/courses")
    public ApiResponse<Course> createCourse(@RequestParam Long teacherId, 
                                            @Valid @RequestBody CourseCreateRequest request) {
        Course course = teacherCourseService.createCourse(teacherId, request);
        return ApiResponse.success(course);
    }

    @GetMapping("/courses")
    public ApiResponse<List<Course>> getCoursesByTeacherId(@RequestParam Long teacherId) {
        List<Course> courses = teacherCourseService.getCoursesByTeacherId(teacherId);
        return ApiResponse.success(courses);
    }

    @PutMapping("/courses/{courseId}")
    public ApiResponse<Course> updateCourse(@RequestParam Long teacherId, 
                                            @PathVariable Long courseId,
                                            @Valid @RequestBody CourseCreateRequest request) {
        Course course = teacherCourseService.updateCourse(teacherId, courseId, request);
        return ApiResponse.success(course);
    }

    @DeleteMapping("/courses/{courseId}")
    public ApiResponse<Void> deleteCourse(@RequestParam Long teacherId, @PathVariable Long courseId) {
        teacherCourseService.deleteCourse(teacherId, courseId);
        return ApiResponse.success("删除成功");
    }

    @PostMapping("/daily-summary")
    public ApiResponse<DailySummary> createDailySummary(@RequestParam Long teacherId,
                                                         @RequestParam Long courseId,
                                                         @RequestParam String content,
                                                         @RequestParam String summaryDate) {
        DailySummary summary = dailySummaryService.createDailySummary(teacherId, courseId, content, summaryDate);
        return ApiResponse.success(summary);
    }

    @GetMapping("/daily-summary/course/{courseId}")
    public ApiResponse<List<DailySummary>> getDailySummariesByCourse(@PathVariable Long courseId) {
        List<DailySummary> summaries = dailySummaryService.getDailySummariesByCourse(courseId);
        return ApiResponse.success(summaries);
    }

    @GetMapping("/students/course/{courseId}")
    public ApiResponse<List<Enrollment>> getStudentsByCourse(@PathVariable Long courseId) {
        List<Enrollment> enrollments = dailySummaryService.getStudentsByCourse(courseId);
        return ApiResponse.success(enrollments);
    }

    @GetMapping("/notifications")
    public ApiResponse<Map<String, Object>> getNotifications(@RequestParam Long teacherId) {
        int unreadCount = messageService.getUnreadCount(teacherId);
        List<Message> messages = messageService.getMessages(teacherId, 0, 20);
        
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", unreadCount);
        result.put("messages", messages);
        
        return ApiResponse.success(result);
    }

}
