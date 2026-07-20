package com.angli.online.controller;

import com.angli.online.dto.request.CourseApplyRequest;
import com.angli.online.dto.request.CourseCreateRequest;
import com.angli.online.dto.request.PushNotificationRequest;
import com.angli.online.dto.response.ApiResponse;
import com.angli.online.entity.Course;
import com.angli.online.entity.CourseApplication;
import com.angli.online.entity.CourseChapter;
import com.angli.online.entity.DailySummary;
import com.angli.online.entity.Enrollment;
import com.angli.online.entity.Message;
import com.angli.online.entity.PushRecord;
import com.angli.online.entity.User;
import com.angli.online.mapper.EnrollmentMapper;
import com.angli.online.mapper.MessageMapper;
import com.angli.online.mapper.PushRecordMapper;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.TeacherCourseService;
import com.angli.online.service.DailySummaryService;
import com.angli.online.service.MessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

  private final TeacherCourseService teacherCourseService;
  private final DailySummaryService dailySummaryService;
  private final MessageService messageService;
  private final UserMapper userMapper;
  private final EnrollmentMapper enrollmentMapper;
  private final MessageMapper messageMapper;
  private final PushRecordMapper pushRecordMapper;

  public TeacherController(TeacherCourseService teacherCourseService,
      DailySummaryService dailySummaryService,
      MessageService messageService,
      UserMapper userMapper,
      EnrollmentMapper enrollmentMapper,
      MessageMapper messageMapper,
      PushRecordMapper pushRecordMapper) {
    this.teacherCourseService = teacherCourseService;
    this.dailySummaryService = dailySummaryService;
    this.messageService = messageService;
    this.userMapper = userMapper;
    this.enrollmentMapper = enrollmentMapper;
    this.messageMapper = messageMapper;
    this.pushRecordMapper = pushRecordMapper;
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

  @PostMapping("/courses/apply")
  public ApiResponse<CourseApplication> applyCourse(@RequestParam Long teacherId,
      @RequestBody CourseApplyRequest request) {
    CourseApplication application = teacherCourseService.applyCourse(teacherId, request);
    return ApiResponse.success(application);
  }

  @GetMapping("/courses/apply/records")
  public ApiResponse<List<CourseApplication>> getApplyRecords(@RequestParam Long teacherId) {
    List<CourseApplication> records = teacherCourseService.getApplyRecords(teacherId);
    return ApiResponse.success(records);
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

  @GetMapping("/course/{courseId}/parents")
  public ApiResponse<List<Map<String, Object>>> getCourseParents(@PathVariable Long courseId) {
    List<Enrollment> enrollments = enrollmentMapper.selectList(new LambdaQueryWrapper<Enrollment>()
        .eq(Enrollment::getCourseId, courseId));

    List<Map<String, Object>> parents = new java.util.ArrayList<>();
    for (Enrollment enrollment : enrollments) {
      User parent = userMapper.selectById(enrollment.getUserId());
      if (parent != null) {
        Map<String, Object> parentInfo = new HashMap<>();
        parentInfo.put("id", parent.getId());
        parentInfo.put("name", parent.getRealName());
        parentInfo.put("phone", parent.getPhone());
        parentInfo.put("courseId", courseId);
        parents.add(parentInfo);
      }
    }

    return ApiResponse.success(parents);
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

  @GetMapping("/home")
  public ApiResponse<Map<String, Object>> getTeacherHomeData(@RequestParam Long teacherId) {
    Map<String, Object> data = teacherCourseService.getTeacherHomeData(teacherId);
    return ApiResponse.success(data);
  }

  @GetMapping("/courses/{courseId}/chapters")
  public ApiResponse<List<CourseChapter>> getCourseChapters(@PathVariable Long courseId) {
    List<CourseChapter> chapters = teacherCourseService.getCourseChapters(courseId);
    return ApiResponse.success(chapters);
  }

  @PutMapping("/courses/{courseId}/chapters/{chapterId}/toggle")
  public ApiResponse<Void> toggleChapterComplete(@PathVariable Long courseId, @PathVariable Long chapterId) {
    teacherCourseService.toggleChapterComplete(courseId, chapterId);
    return ApiResponse.success("更新成功");
  }

  @PostMapping("/push")
  public ApiResponse<Void> pushNotification(@RequestParam Long teacherId,
      @RequestBody PushNotificationRequest request) {

    User teacher = userMapper.selectById(teacherId);
    if (teacher == null) {
      throw new RuntimeException("教师不存在");
    }

    Course course = teacherCourseService.getCourseById(request.getCourseId());
    if (course == null) {
      throw new RuntimeException("课程不存在");
    }

    List<Enrollment> enrollments = enrollmentMapper.selectList(new LambdaQueryWrapper<Enrollment>()
        .eq(Enrollment::getCourseId, request.getCourseId()));

    for (Enrollment enrollment : enrollments) {
      Message message = new Message();
      message.setUserId(enrollment.getUserId());
      message.setType("DAILY_SUMMARY");
      message.setTitle(course.getTitle() + " - 今日总结");

      StringBuilder content = new StringBuilder();
      content.append("📖 **今日课程总结**\n\n");
      if (request.getSummary() != null && !request.getSummary().isEmpty()) {
        content.append("课堂内容：\n").append(request.getSummary()).append("\n\n");
      }
      if (request.getHomework() != null && !request.getHomework().isEmpty()) {
        content.append("📝 **今日作业**\n").append(request.getHomework()).append("\n\n");
      }
      if (request.getGoodStudents() != null && !request.getGoodStudents().isEmpty()) {
        content.append("⭐ **表现优秀学生**\n").append(request.getGoodStudents()).append("\n\n");
      }
      content.append("发送老师：").append(teacher.getRealName());

      message.setContent(content.toString());
      message.setRelatedId(request.getCourseId());
      message.setIsRead(0);
      message.setCreatedAt(LocalDateTime.now());
      message.setUpdatedAt(LocalDateTime.now());

      messageMapper.insert(message);
    }

    String targetUserIds = enrollments.stream()
        .map(e -> String.valueOf(e.getUserId()))
        .reduce((a, b) -> a + "," + b)
        .orElse("");

    PushRecord pushRecord = new PushRecord();
    pushRecord.setTeacherId(teacherId);
    pushRecord.setCourseId(request.getCourseId());
    pushRecord.setCourseTitle(course.getTitle());
    pushRecord.setSummary(request.getSummary());
    pushRecord.setHomework(request.getHomework());
    pushRecord.setGoodStudents(request.getGoodStudents());
    pushRecord.setTargetUserIds(targetUserIds);
    pushRecord.setTargetCount(enrollments.size());
    pushRecord.setCreatedAt(LocalDateTime.now());
    pushRecordMapper.insert(pushRecord);

    return ApiResponse.success("推送成功");
  }

  @GetMapping("/push/records")
  public ApiResponse<List<PushRecord>> getPushRecords(@RequestParam Long teacherId) {
    List<PushRecord> records = pushRecordMapper.selectList(new LambdaQueryWrapper<PushRecord>()
        .eq(PushRecord::getTeacherId, teacherId)
        .orderByDesc(PushRecord::getCreatedAt));
    return ApiResponse.success(records);
  }

  @GetMapping("/push/records/{id}")
  public ApiResponse<Map<String, Object>> getPushRecordDetail(@PathVariable Long id, @RequestParam Long teacherId) {
    PushRecord record = pushRecordMapper.selectById(id);
    if (record == null || !record.getTeacherId().equals(teacherId)) {
      throw new RuntimeException("推送记录不存在");
    }

    Map<String, Object> result = new HashMap<>();
    result.put("record", record);

    if (record.getTargetUserIds() != null && !record.getTargetUserIds().isEmpty()) {
      String[] userIdArray = record.getTargetUserIds().split(",");
      List<User> targetUsers = userMapper.selectBatchIds(java.util.Arrays.asList(userIdArray));
      result.put("targetUsers", targetUsers);
    }

    return ApiResponse.success(result);
  }

  @GetMapping("/settings")
  public ApiResponse<Map<String, Object>> getTeacherSettings(@RequestParam Long teacherId) {
    User user = userMapper.selectById(teacherId);
    Map<String, Object> settings = new HashMap<>();
    if (user != null) {
      settings.put("id", user.getId());
      settings.put("realName", user.getRealName());
      settings.put("phone", user.getPhone());
      if (user.getTags() != null && !user.getTags().isEmpty()) {
        settings.put("tags", user.getTags().split(","));
      } else {
        settings.put("tags", new String[0]);
      }
    }
    return ApiResponse.success(settings);
  }

  @PutMapping("/settings")
  public ApiResponse<User> updateTeacherSettings(@RequestParam Long teacherId,
      @RequestBody Map<String, Object> data) {
    User user = userMapper.selectById(teacherId);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    if (data.containsKey("realName")) {
      user.setRealName((String) data.get("realName"));
    }
    if (data.containsKey("avatar")) {
      user.setAvatar((String) data.get("avatar"));
    }
    if (data.containsKey("tags")) {
      Object tagsObj = data.get("tags");
      if (tagsObj instanceof List) {
        user.setTags(String.join(",", ((List<String>) tagsObj).toArray(new String[0])));
      } else if (tagsObj instanceof String) {
        user.setTags((String) tagsObj);
      }
    }
    userMapper.updateById(user);
    return ApiResponse.success(user);
  }

}
