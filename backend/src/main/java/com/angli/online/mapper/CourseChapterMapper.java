package com.angli.online.mapper;

import com.angli.online.entity.CourseChapter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseChapterMapper extends BaseMapper<CourseChapter> {

    @Select("SELECT COALESCE(COUNT(*), 0) FROM course_chapters cc " +
            "JOIN enrollments e ON cc.course_id = e.course_id " +
            "WHERE e.user_id = #{userId} AND cc.is_completed = 1")
    Integer countCompletedChapters(@Param("userId") Long userId);

}