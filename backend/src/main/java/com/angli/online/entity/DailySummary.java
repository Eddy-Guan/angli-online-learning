package com.angli.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_summaries")
public class DailySummary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private Long courseId;

    private String content;

    private LocalDate summaryDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}