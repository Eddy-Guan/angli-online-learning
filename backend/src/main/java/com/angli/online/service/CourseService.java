package com.angli.online.service;

import com.angli.online.dto.response.CourseResponse;
import com.angli.online.entity.Course;

import java.util.List;

public interface CourseService {

    List<CourseResponse> getRecommendedCourses();

    List<CourseResponse> getHotCourses();

    List<CourseResponse> getAllPublishedCourses();

    List<CourseResponse> searchCourses(String keyword);

    CourseResponse getCourseById(Long id);

    boolean save(Course entity);

    boolean saveOrUpdate(Course entity);

    boolean removeById(Long id);

}