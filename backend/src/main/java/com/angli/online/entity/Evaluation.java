package com.angli.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluations")
public class Evaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long courseId;

    private Long studentId;

    private Integer rating;

    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}