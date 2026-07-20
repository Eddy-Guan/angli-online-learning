package com.angli.online.controller;

import com.angli.online.dto.request.CourseCreateRequest;
import com.angli.online.dto.response.ApiResponse;
import com.angli.online.entity.Course;
import com.angli.online.entity.CourseApplication;
import com.angli.online.entity.CourseChapter;
import com.angli.online.entity.Evaluation;
import com.angli.online.entity.Notification;
import com.angli.online.entity.Order;
import com.angli.online.entity.User;
import com.angli.online.mapper.CourseApplicationMapper;
import com.angli.online.mapper.CourseChapterMapper;
import com.angli.online.mapper.CourseMapper;
import com.angli.online.mapper.EvaluationMapper;
import com.angli.online.mapper.OrderMapper;
import com.angli.online.mapper.StudentMapper;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.MessageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final UserMapper userMapper;
  private final CourseMapper courseMapper;
  private final EvaluationMapper evaluationMapper;
  private final StudentMapper studentMapper;
  private final OrderMapper orderMapper;
  private final MessageService messageService;

  private final CourseApplicationMapper courseApplicationMapper;
  private final CourseChapterMapper courseChapterMapper;
  private final ObjectMapper objectMapper;

  public AdminController(UserMapper userMapper, CourseMapper courseMapper,
      EvaluationMapper evaluationMapper, StudentMapper studentMapper,
      OrderMapper orderMapper, MessageService messageService,
      CourseApplicationMapper courseApplicationMapper,
      CourseChapterMapper courseChapterMapper, ObjectMapper objectMapper) {
    this.userMapper = userMapper;
    this.courseMapper = courseMapper;
    this.evaluationMapper = evaluationMapper;
    this.studentMapper = studentMapper;
    this.orderMapper = orderMapper;
    this.messageService = messageService;
    this.courseApplicationMapper = courseApplicationMapper;
    this.courseChapterMapper = courseChapterMapper;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/dashboard")
  public ApiResponse<Map<String, Object>> getDashboard() {
    Map<String, Object> dashboard = new HashMap<>();

    Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
        .eq(User::getStatus, "ACTIVE"));
    dashboard.put("totalUsers", totalUsers);

    Long activeTeachers = userMapper.selectCount(new LambdaQueryWrapper<User>()
        .eq(User::getRole, "TEACHER")
        .eq(User::getStatus, "ACTIVE"));
    dashboard.put("activeTeachers", activeTeachers);

    Long pendingTeachers = userMapper.selectCount(new LambdaQueryWrapper<User>()
        .eq(User::getRole, "TEACHER")
        .eq(User::getStatus, "PENDING"));
    dashboard.put("pendingTeachers", pendingTeachers);

    Long totalStudents = studentMapper.selectCount(null);
    dashboard.put("totalStudents", totalStudents);

    List<Evaluation> allEvaluations = evaluationMapper.selectList(null);
    if (!allEvaluations.isEmpty()) {
      int totalRating = allEvaluations.stream().mapToInt(Evaluation::getRating).sum();
      double avgRating = Math.round((double) totalRating / allEvaluations.size() * 10) / 10.0;
      dashboard.put("averageRating", avgRating);
    } else {
      dashboard.put("averageRating", 0.0);
    }

    YearMonth currentMonth = YearMonth.now();
    LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
    LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

    BigDecimal monthRevenue = orderMapper.selectList(new LambdaQueryWrapper<Order>()
        .eq(Order::getStatus, "PAID")
        .ge(Order::getCreatedAt, monthStart)
        .le(Order::getCreatedAt, monthEnd))
        .stream()
        .map(Order::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    dashboard.put("monthlyRevenue", monthRevenue);

    return ApiResponse.success(dashboard);
  }

  @GetMapping("/stats")
  public ApiResponse<Map<String, Object>> getStats() {
    Map<String, Object> stats = new HashMap<>();

    Long totalUsers = userMapper.selectCount(null);
    stats.put("totalUsers", totalUsers);

    Long totalCourses = courseMapper.selectCount(null);
    stats.put("totalCourses", totalCourses);

    Long pendingCourses = courseMapper.selectCount(new LambdaQueryWrapper<Course>()
        .eq(Course::getStatus, "PENDING"));
    stats.put("pendingCourses", pendingCourses);

    return ApiResponse.success(stats);
  }

  @GetMapping("/users")
  public ApiResponse<List<User>> getUsers(@RequestParam(required = false) String role,
      @RequestParam(required = false) String status) {
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    if (role != null) {
      wrapper.eq(User::getRole, role);
    }
    if (status != null) {
      wrapper.eq(User::getStatus, status);
    }
    wrapper.orderByDesc(User::getCreatedAt);
    return ApiResponse.success(userMapper.selectList(wrapper));
  }

  @GetMapping("/users/{id}")
  public ApiResponse<User> getUserById(@PathVariable Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    return ApiResponse.success(user);
  }

  @PutMapping("/users/{id}/approve")
  public ApiResponse<User> approveUser(@PathVariable Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    if (!"TEACHER".equals(user.getRole())) {
      throw new RuntimeException("只有教师需要审核");
    }
    user.setStatus("ACTIVE");
    userMapper.updateById(user);
    return ApiResponse.success(user);
  }

  @PutMapping("/users/{id}/reject")
  public ApiResponse<User> rejectUser(@PathVariable Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    if (!"TEACHER".equals(user.getRole())) {
      throw new RuntimeException("只有教师需要审核");
    }
    user.setStatus("DISABLED");
    userMapper.updateById(user);
    return ApiResponse.success(user);
  }

  @DeleteMapping("/users/{id}")
  public ApiResponse<Void> deleteUser(@PathVariable Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    user.setStatus("DISABLED");
    userMapper.updateById(user);
    return ApiResponse.success("删除成功");
  }

  @GetMapping("/courses")
  public ApiResponse<List<Course>> getCourses(@RequestParam(required = false) String status) {
    LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
    if (status != null) {
      wrapper.eq(Course::getStatus, status);
    }
    wrapper.orderByDesc(Course::getCreatedAt);
    return ApiResponse.success(courseMapper.selectList(wrapper));
  }

  @PostMapping("/courses")
  public ApiResponse<Course> createCourse(@Valid @RequestBody CourseCreateRequest request) {
    Course course = new Course();
    course.setTitle(request.getTitle());
    course.setDescription(request.getDescription());
    course.setCategory(request.getCategory());
    course.setPrice(request.getPrice());
    course.setOriginalPrice(request.getOriginalPrice());
    course.setTotalHours(request.getTotalHours());
    course.setCoverImage(request.getCoverImage());
    course.setStatus("PENDING");
    course.setIsRecommended(0);
    course.setEnrollmentCount(0);
    course.setFavoriteCount(0);
    course.setTeacherId(1L);

    courseMapper.insert(course);
    return ApiResponse.success(course);
  }

  @PutMapping("/courses/{id}/publish")
  public ApiResponse<Course> publishCourse(@PathVariable Long id) {
    Course course = courseMapper.selectById(id);
    if (course == null) {
      throw new RuntimeException("课程不存在");
    }
    course.setStatus("PUBLISHED");
    courseMapper.updateById(course);
    return ApiResponse.success(course);
  }

  @PutMapping("/courses/{id}/unpublish")
  public ApiResponse<Course> unpublishCourse(@PathVariable Long id) {
    Course course = courseMapper.selectById(id);
    if (course == null) {
      throw new RuntimeException("课程不存在");
    }
    course.setStatus("ENDED");
    courseMapper.updateById(course);
    return ApiResponse.success(course);
  }

  @PutMapping("/courses/{id}/recommend")
  public ApiResponse<Course> toggleRecommend(@PathVariable Long id, @RequestParam Boolean recommend) {
    Course course = courseMapper.selectById(id);
    if (course == null) {
      throw new RuntimeException("课程不存在");
    }
    course.setIsRecommended(recommend ? 1 : 0);
    courseMapper.updateById(course);
    return ApiResponse.success(course);
  }

  @PutMapping("/courses/{id}/approve")
  public ApiResponse<Course> approveCourse(@PathVariable Long id) {
    Course course = courseMapper.selectById(id);
    if (course == null) {
      throw new RuntimeException("课程不存在");
    }
    course.setStatus("PUBLISHED");
    courseMapper.updateById(course);
    return ApiResponse.success(course);
  }

  @PutMapping("/courses/{id}/reject")
  public ApiResponse<Course> rejectCourse(@PathVariable Long id,
      @RequestBody(required = false) Map<String, String> body) {
    Course course = courseMapper.selectById(id);
    if (course == null) {
      throw new RuntimeException("课程不存在");
    }
    course.setStatus("REJECTED");
    courseMapper.updateById(course);
    return ApiResponse.success(course);
  }

  @PutMapping("/users/{id}/status")
  public ApiResponse<User> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
    User user = userMapper.selectById(id);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    String status = body.get("status");
    if ("disabled".equalsIgnoreCase(status)) {
      user.setStatus("DISABLED");
    } else {
      user.setStatus("ACTIVE");
    }
    userMapper.updateById(user);
    return ApiResponse.success(user);
  }

  @DeleteMapping("/courses/{id}")
  public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
    Course course = courseMapper.selectById(id);
    if (course == null) {
      throw new RuntimeException("课程不存在");
    }
    courseMapper.deleteById(id);
    return ApiResponse.success("删除成功");
  }

  @GetMapping("/notifications")
  public ApiResponse<List<Notification>> getNotifications() {
    return ApiResponse.success(messageService.getAllNotifications());
  }

  @PostMapping("/notifications")
  public ApiResponse<Notification> createNotification(@RequestParam String title,
      @RequestParam String content,
      @RequestParam String targetRole) {
    Notification notification = messageService.createNotification(title, content, targetRole);
    return ApiResponse.success(notification);
  }

  @PostMapping("/notifications/{id}/publish")
  public ApiResponse<Void> publishNotification(@PathVariable Long id) {
    messageService.publishNotification(id);
    return ApiResponse.success("发布成功");
  }

  @PostMapping("/notices/publish")
  public ApiResponse<Void> publishNotice(@RequestBody Map<String, Object> body) {
    String title = (String) body.get("title");
    String content = (String) body.get("content");
    Boolean pushToUsers = (Boolean) body.getOrDefault("pushToUsers", true);
    Boolean pushToTeachers = (Boolean) body.getOrDefault("pushToTeachers", false);

    if (pushToUsers) {
      Notification notification = new Notification();
      notification.setTitle(title);
      notification.setContent(content);
      notification.setTargetRole("PARENT");
      notification.setIsPublished(1);
      notification.setPublishedAt(LocalDateTime.now());
      messageService.publishNotification(notification);
    }

    if (pushToTeachers) {
      Notification notification = new Notification();
      notification.setTitle(title);
      notification.setContent(content);
      notification.setTargetRole("TEACHER");
      notification.setIsPublished(1);
      notification.setPublishedAt(LocalDateTime.now());
      messageService.publishNotification(notification);
    }

    return ApiResponse.success("公告发布成功");
  }

  @GetMapping("/course-applications")
  public ApiResponse<List<CourseApplication>> getCourseApplications(
      @RequestParam(required = false) String status) {
    LambdaQueryWrapper<CourseApplication> wrapper = new LambdaQueryWrapper<>();
    if (status != null) {
      wrapper.eq(CourseApplication::getStatus, status);
    }
    wrapper.orderByDesc(CourseApplication::getAppliedAt);
    return ApiResponse.success(courseApplicationMapper.selectList(wrapper));
  }

  @GetMapping("/course-applications/{id}")
  public ApiResponse<CourseApplication> getCourseApplication(@PathVariable Long id) {
    CourseApplication application = courseApplicationMapper.selectById(id);
    if (application == null) {
      throw new RuntimeException("课程申请不存在");
    }
    return ApiResponse.success(application);
  }

  @PutMapping("/course-applications/{id}/approve")
  public ApiResponse<CourseApplication> approveCourseApplication(
      @PathVariable Long id,
      @RequestParam(required = false, defaultValue = "false") Boolean recommend) {
    CourseApplication application = courseApplicationMapper.selectById(id);
    if (application == null) {
      throw new RuntimeException("课程申请不存在");
    }
    if (!"PENDING".equals(application.getStatus())) {
      throw new RuntimeException("只能审核待审核的申请");
    }

    Course course = new Course();
    course.setTeacherId(application.getTeacherId());
    course.setTitle(application.getTitle());
    course.setCategory(application.getSubject());
    course.setDescription(application.getDescription());
    course.setTotalHours(application.getTotalHours());
    course.setStatus("PUBLISHED");
    course.setIsRecommended(recommend ? 1 : 0);
    course.setCoverImage("/api/images/default-course.svg");
    course.setEnrollmentCount(0);
    course.setFavoriteCount(0);
    courseMapper.insert(course);

    if (application.getChaptersJson() != null && !application.getChaptersJson().isEmpty()) {
      try {
        List<Map<String, String>> chapters = objectMapper.readValue(
            application.getChaptersJson(),
            new TypeReference<List<Map<String, String>>>() {
            });
        for (int i = 0; i < chapters.size(); i++) {
          CourseChapter chapter = new CourseChapter();
          chapter.setCourseId(course.getId());
          chapter.setChapterName(chapters.get(i).get("title"));
          chapter.setChapterOrder(i + 1);
          chapter.setContent(chapters.get(i).get("description"));
          chapter.setDuration("45");
          courseChapterMapper.insert(chapter);
        }
      } catch (Exception e) {
        throw new RuntimeException("章节数据解析失败");
      }
    }

    application.setStatus("APPROVED");
    application.setReviewedAt(LocalDateTime.now());
    courseApplicationMapper.updateById(application);

    return ApiResponse.success(application);
  }

  @PutMapping("/course-applications/{id}/reject")
  public ApiResponse<CourseApplication> rejectCourseApplication(
      @PathVariable Long id,
      @RequestBody(required = false) Map<String, String> body) {
    CourseApplication application = courseApplicationMapper.selectById(id);
    if (application == null) {
      throw new RuntimeException("课程申请不存在");
    }
    if (!"PENDING".equals(application.getStatus())) {
      throw new RuntimeException("只能审核待审核的申请");
    }

    application.setStatus("REJECTED");
    application.setRejectReason(body != null ? body.get("reason") : null);
    application.setReviewedAt(LocalDateTime.now());
    courseApplicationMapper.updateById(application);

    return ApiResponse.success(application);
  }

}
