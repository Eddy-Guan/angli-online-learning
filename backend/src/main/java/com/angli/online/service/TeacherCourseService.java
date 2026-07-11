package com.angli.online.service;

import com.angli.online.dto.request.CourseCreateRequest;
import com.angli.online.entity.Course;

import java.util.List;

public interface TeacherCourseService {

    Course createCourse(Long teacherId, CourseCreateRequest request);

    List<Course> getCoursesByTeacherId(Long teacherId);

    Course updateCourse(Long teacherId, Long courseId, CourseCreateRequest request);

    void deleteCourse(Long teacherId, Long courseId);

}
