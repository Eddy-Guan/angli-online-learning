package com.angli.online.service.impl;

import com.angli.online.dto.request.CourseApplyRequest;
import com.angli.online.dto.request.CourseCreateRequest;
import com.angli.online.entity.Course;
import com.angli.online.entity.CourseApplication;
import com.angli.online.entity.CourseChapter;
import com.angli.online.entity.Enrollment;
import com.angli.online.entity.User;
import com.angli.online.mapper.CourseApplicationMapper;
import com.angli.online.mapper.CourseChapterMapper;
import com.angli.online.mapper.CourseMapper;
import com.angli.online.mapper.EnrollmentMapper;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.TeacherCourseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeacherCourseServiceImpl implements TeacherCourseService {

  private final CourseMapper courseMapper;
  private final UserMapper userMapper;
  private final CourseChapterMapper courseChapterMapper;
  private final EnrollmentMapper enrollmentMapper;

  private final CourseApplicationMapper courseApplicationMapper;
  private final ObjectMapper objectMapper;

  public TeacherCourseServiceImpl(CourseMapper courseMapper, UserMapper userMapper,
      CourseChapterMapper courseChapterMapper, EnrollmentMapper enrollmentMapper,
      CourseApplicationMapper courseApplicationMapper, ObjectMapper objectMapper) {
    this.courseMapper = courseMapper;
    this.userMapper = userMapper;
    this.courseChapterMapper = courseChapterMapper;
    this.enrollmentMapper = enrollmentMapper;
    this.courseApplicationMapper = courseApplicationMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public Course createCourse(Long teacherId, CourseCreateRequest request) {
    User teacher = userMapper.selectById(teacherId);
    if (teacher == null) {
      throw new RuntimeException("教师不存在");
    }
    if (!"TEACHER".equals(teacher.getRole())) {
      throw new RuntimeException("当前用户不是教师");
    }

    Course course = new Course();
    course.setTeacherId(teacherId);
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

    courseMapper.insert(course);
    return course;
  }

  @Override
  public List<Course> getCoursesByTeacherId(Long teacherId) {
    return courseMapper.selectList(new LambdaQueryWrapper<Course>()
        .eq(Course::getTeacherId, teacherId)
        .orderByDesc(Course::getCreatedAt));
  }

  @Override
  public Course getCourseById(Long courseId) {
    return courseMapper.selectById(courseId);
  }

  @Override
  public Course updateCourse(Long teacherId, Long courseId, CourseCreateRequest request) {
    Course course = courseMapper.selectById(courseId);
    if (course == null) {
      throw new RuntimeException("课程不存在");
    }
    if (!course.getTeacherId().equals(teacherId)) {
      throw new RuntimeException("无权修改该课程");
    }

    course.setTitle(request.getTitle());
    course.setDescription(request.getDescription());
    course.setCategory(request.getCategory());
    course.setPrice(request.getPrice());
    course.setOriginalPrice(request.getOriginalPrice());
    course.setTotalHours(request.getTotalHours());
    course.setCoverImage(request.getCoverImage());

    courseMapper.updateById(course);
    return course;
  }

  @Override
  public void deleteCourse(Long teacherId, Long courseId) {
    Course course = courseMapper.selectById(courseId);
    if (course == null) {
      throw new RuntimeException("课程不存在");
    }
    if (!course.getTeacherId().equals(teacherId)) {
      throw new RuntimeException("无权删除该课程");
    }
    if ("PUBLISHED".equals(course.getStatus())) {
      throw new RuntimeException("已上架课程无法删除");
    }
    courseMapper.deleteById(courseId);
  }

  @Override
  public List<CourseChapter> getCourseChapters(Long courseId) {
    return courseChapterMapper.selectList(new LambdaQueryWrapper<CourseChapter>()
        .eq(CourseChapter::getCourseId, courseId)
        .orderByAsc(CourseChapter::getChapterOrder));
  }

  @Override
  public void toggleChapterComplete(Long courseId, Long chapterId) {
    CourseChapter chapter = courseChapterMapper.selectById(chapterId);
    if (chapter == null) {
      throw new RuntimeException("课时不存在");
    }
    if (!chapter.getCourseId().equals(courseId)) {
      throw new RuntimeException("课时不属于该课程");
    }
    chapter.setIsCompleted(chapter.getIsCompleted() == 1 ? 0 : 1);
    courseChapterMapper.updateById(chapter);
  }

  @Override
  public Map<String, Object> getTeacherHomeData(Long teacherId) {
    Map<String, Object> result = new HashMap<>();

    List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
        .eq(Course::getTeacherId, teacherId));
    result.put("courseCount", courses.size());

    int totalStudents = 0;
    for (Course course : courses) {
      Long count = enrollmentMapper.selectCount(new LambdaQueryWrapper<Enrollment>()
          .eq(Enrollment::getCourseId, course.getId()));
      totalStudents += count.intValue();
    }
    result.put("studentCount", totalStudents);

    result.put("averageRating", 4.8);
    result.put("attendanceRate", 95);
    result.put("homeworkSubmitRate", 92);
    result.put("parentRating", 4.9);

    User teacher = userMapper.selectById(teacherId);
    if (teacher != null) {
      result.put("teacherName", teacher.getRealName());
      if (teacher.getTags() != null && !teacher.getTags().isEmpty()) {
        result.put("tags", teacher.getTags().split(","));
      }
    }

    return result;
  }

  @Override
  public CourseApplication applyCourse(Long teacherId, CourseApplyRequest request) {
    User teacher = userMapper.selectById(teacherId);
    if (teacher == null) {
      throw new RuntimeException("教师不存在");
    }
    if (!"TEACHER".equals(teacher.getRole())) {
      throw new RuntimeException("当前用户不是教师");
    }

    CourseApplication application = new CourseApplication();
    application.setTeacherId(teacherId);
    application.setTitle(request.getTitle());
    application.setSubject(request.getSubject());
    application.setGrade(request.getGrade());
    application.setTotalHours(request.getTotalHours());
    application.setDescription(request.getDescription());

    try {
      String chaptersJson = objectMapper.writeValueAsString(request.getChapters());
      application.setChaptersJson(chaptersJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("章节数据序列化失败");
    }

    application.setStatus("PENDING");
    application.setAppliedAt(LocalDateTime.now());

    courseApplicationMapper.insert(application);
    return application;
  }

  @Override
  public List<CourseApplication> getApplyRecords(Long teacherId) {
    return courseApplicationMapper.selectList(new LambdaQueryWrapper<CourseApplication>()
        .eq(CourseApplication::getTeacherId, teacherId)
        .orderByDesc(CourseApplication::getAppliedAt));
  }

}
