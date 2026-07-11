package com.angli.online.service.impl;

import com.angli.online.dto.response.CourseResponse;
import com.angli.online.entity.Course;
import com.angli.online.entity.User;
import com.angli.online.mapper.CourseChapterMapper;
import com.angli.online.mapper.CourseMapper;
import com.angli.online.mapper.EvaluationMapper;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.CourseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

  private final CourseMapper courseMapper;
  private final UserMapper userMapper;
  private final CourseChapterMapper chapterMapper;
  private final EvaluationMapper evaluationMapper;

  public CourseServiceImpl(CourseMapper courseMapper, UserMapper userMapper,
      CourseChapterMapper chapterMapper, EvaluationMapper evaluationMapper) {
    this.courseMapper = courseMapper;
    this.userMapper = userMapper;
    this.chapterMapper = chapterMapper;
    this.evaluationMapper = evaluationMapper;
  }

  @Override
  public List<CourseResponse> getRecommendedCourses() {
    List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
        .eq(Course::getStatus, "PUBLISHED")
        .eq(Course::getIsRecommended, 1)
        .orderByDesc(Course::getUpdatedAt)
        .last("LIMIT 4"));
    return convertToResponse(courses);
  }

  @Override
  public List<CourseResponse> getHotCourses() {
    List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
        .eq(Course::getStatus, "PUBLISHED")
        .orderByDesc(Course::getEnrollmentCount)
        .last("LIMIT 20"));
    return convertToResponse(courses);
  }

  @Override
  public List<CourseResponse> getAllPublishedCourses() {
    List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
        .eq(Course::getStatus, "PUBLISHED")
        .orderByDesc(Course::getCreatedAt));
    return convertToResponse(courses);
  }

  @Override
  public List<CourseResponse> searchCourses(String keyword) {
    List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
        .eq(Course::getStatus, "PUBLISHED")
        .and(wrapper -> wrapper
            .like(Course::getTitle, keyword)
            .or()
            .like(Course::getDescription, keyword)
            .or()
            .like(Course::getCategory, keyword)));
    return convertToResponse(courses);
  }

  @Override
    public CourseResponse getCourseById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        return convertToResponse(course);
    }

@Override
    public boolean save(Course entity) {
        return courseMapper.insert(entity) > 0;
    }

    @Override
    public boolean saveOrUpdate(Course entity) {
        if (entity.getId() != null && courseMapper.selectById(entity.getId()) != null) {
            return courseMapper.updateById(entity) > 0;
        }
        return courseMapper.insert(entity) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return courseMapper.deleteById(id) > 0;
    }

    private List<CourseResponse> convertToResponse(List<Course> courses) {
        return courses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private CourseResponse convertToResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setCoverImage(course.getCoverImage());
        response.setPrice(course.getPrice());
        response.setOriginalPrice(course.getOriginalPrice());
        response.setDuration(course.getTotalHours());
        response.setCategory(course.getCategory());
        response.setStatus(course.getStatus());
        response.setStudents(course.getEnrollmentCount());
        response.setCreatedAt(course.getCreatedAt().toString());

        User teacher = userMapper.selectById(course.getTeacherId());
        if (teacher != null) {
            response.setTeacherName(teacher.getRealName());
            response.setTeacherAvatar(teacher.getAvatar());
        }

        Long lessons = chapterMapper.selectCount(new LambdaQueryWrapper<com.angli.online.entity.CourseChapter>()
                .eq(com.angli.online.entity.CourseChapter::getCourseId, course.getId()));
        response.setLessons(lessons.intValue());

        return response;
    }

}