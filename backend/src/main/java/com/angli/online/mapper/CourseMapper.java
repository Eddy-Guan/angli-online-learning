package com.angli.online.mapper;

import com.angli.online.dto.response.CourseResponse;
import com.angli.online.entity.Course;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("SELECT c.id, c.title, c.description, c.cover_image as coverImage, c.price, c.original_price as originalPrice, " +
            "c.category, c.status, c.enrollment_count as students, c.created_at as createdAt, " +
            "u.real_name as teacherName, u.avatar as teacherAvatar, " +
            "(SELECT COALESCE(AVG(e.rating), 0) FROM evaluations e WHERE e.course_id = c.id) as rating, " +
            "(SELECT COUNT(*) FROM course_chapters cc WHERE cc.course_id = c.id) as lessons, " +
            "c.total_hours as duration " +
            "FROM courses c " +
            "LEFT JOIN users u ON c.teacher_id = u.id " +
            "WHERE c.is_recommended = 1 AND c.status = 'PUBLISHED' " +
            "ORDER BY c.recommend_order DESC, c.updated_at DESC " +
            "LIMIT 4")
    List<CourseResponse> selectRecommendedCourses();

    @Select("SELECT c.id, c.title, c.description, c.cover_image as coverImage, c.price, c.original_price as originalPrice, " +
            "c.category, c.status, c.enrollment_count as students, c.created_at as createdAt, " +
            "u.real_name as teacherName, u.avatar as teacherAvatar, " +
            "(SELECT COALESCE(AVG(e.rating), 0) FROM evaluations e WHERE e.course_id = c.id) as rating, " +
            "(SELECT COUNT(*) FROM course_chapters cc WHERE cc.course_id = c.id) as lessons, " +
            "c.total_hours as duration " +
            "FROM courses c " +
            "LEFT JOIN users u ON c.teacher_id = u.id " +
            "WHERE c.status = 'PUBLISHED' " +
            "ORDER BY c.enrollment_count DESC, c.favorite_count DESC")
    List<CourseResponse> selectHotCourses();

    @Select("SELECT c.id, c.title, c.description, c.cover_image as coverImage, c.price, c.original_price as originalPrice, " +
            "c.category, c.status, c.enrollment_count as students, c.created_at as createdAt, " +
            "u.real_name as teacherName, u.avatar as teacherAvatar, " +
            "(SELECT COALESCE(AVG(e.rating), 0) FROM evaluations e WHERE e.course_id = c.id) as rating, " +
            "(SELECT COUNT(*) FROM course_chapters cc WHERE cc.course_id = c.id) as lessons, " +
            "c.total_hours as duration " +
            "FROM courses c " +
            "LEFT JOIN users u ON c.teacher_id = u.id " +
            "WHERE c.status = 'PUBLISHED' " +
            "ORDER BY c.created_at DESC")
    List<CourseResponse> selectAllPublishedCourses();

    @Select("SELECT c.id, c.title, c.description, c.cover_image as coverImage, c.price, c.original_price as originalPrice, " +
            "c.category, c.status, c.enrollment_count as students, c.created_at as createdAt, " +
            "u.real_name as teacherName, u.avatar as teacherAvatar, " +
            "(SELECT COALESCE(AVG(e.rating), 0) FROM evaluations e WHERE e.course_id = c.id) as rating, " +
            "(SELECT COUNT(*) FROM course_chapters cc WHERE cc.course_id = c.id) as lessons, " +
            "c.total_hours as duration " +
            "FROM courses c " +
            "LEFT JOIN users u ON c.teacher_id = u.id " +
            "WHERE c.status = 'PUBLISHED' " +
            "AND (c.title LIKE CONCAT('%', #{keyword}, '%') OR c.description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.category LIKE CONCAT('%', #{keyword}, '%') OR u.real_name LIKE CONCAT('%', #{keyword}, '%'))")
    List<CourseResponse> selectSearchCourses(@Param("keyword") String keyword);

    @Select("SELECT c.id, c.title, c.description, c.cover_image as coverImage, c.price, c.original_price as originalPrice, " +
            "c.category, c.status, c.enrollment_count as students, c.created_at as createdAt, " +
            "u.real_name as teacherName, u.avatar as teacherAvatar, " +
            "(SELECT COALESCE(AVG(e.rating), 0) FROM evaluations e WHERE e.course_id = c.id) as rating, " +
            "(SELECT COUNT(*) FROM course_chapters cc WHERE cc.course_id = c.id) as lessons, " +
            "c.total_hours as duration " +
            "FROM courses c " +
            "LEFT JOIN users u ON c.teacher_id = u.id " +
            "WHERE c.id = #{id}")
    CourseResponse selectCourseById(@Param("id") Long id);

    @Select("SELECT COALESCE(SUM(c.total_hours), 0) FROM courses c " +
            "JOIN enrollments e ON c.id = e.course_id " +
            "WHERE e.user_id = #{userId} AND e.status = 'COMPLETED'")
    Integer sumCompletedCourseHours(@Param("userId") Long userId);

}