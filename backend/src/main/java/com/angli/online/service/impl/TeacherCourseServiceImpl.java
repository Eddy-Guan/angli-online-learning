package com.angli.online.service.impl;

import com.angli.online.dto.request.CourseCreateRequest;
import com.angli.online.entity.Course;
import com.angli.online.entity.User;
import com.angli.online.mapper.CourseMapper;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.TeacherCourseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherCourseServiceImpl implements TeacherCourseService {

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    public TeacherCourseServiceImpl(CourseMapper courseMapper, UserMapper userMapper) {
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
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

}
