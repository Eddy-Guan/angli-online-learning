package com.angli.online.service;

import com.angli.online.dto.request.CourseApplyRequest;
import com.angli.online.dto.request.CourseCreateRequest;
import com.angli.online.entity.Course;
import com.angli.online.entity.CourseApplication;
import com.angli.online.entity.CourseChapter;

import java.util.List;
import java.util.Map;

public interface TeacherCourseService {

  Course createCourse(Long teacherId, CourseCreateRequest request);

  List<Course> getCoursesByTeacherId(Long teacherId);

  Course getCourseById(Long courseId);

  Course updateCourse(Long teacherId, Long courseId, CourseCreateRequest request);

  void deleteCourse(Long teacherId, Long courseId);

  List<CourseChapter> getCourseChapters(Long courseId);

  void toggleChapterComplete(Long courseId, Long chapterId);

  Map<String, Object> getTeacherHomeData(Long teacherId);

  CourseApplication applyCourse(Long teacherId, CourseApplyRequest request);

  List<CourseApplication> getApplyRecords(Long teacherId);

}
