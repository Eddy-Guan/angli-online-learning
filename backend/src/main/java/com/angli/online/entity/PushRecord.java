package com.angli.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("push_records")
public class PushRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private Long courseId;

    private String courseTitle;

    private String summary;

    private String homework;

    private String goodStudents;

    private String targetUserIds;

    private Integer targetCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}