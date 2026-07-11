package com.angli.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_chapters")
public class CourseChapter {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private String chapterName;

    private Integer chapterOrder;

    private String duration;

    private String content;

    private Integer isCompleted;

    private LocalDateTime completedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}