package com.angli.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("courses")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private String title;

    private String description;

    private String category;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer totalHours;

    private String coverImage;

    private String status;

    private Integer isRecommended;

    private Integer recommendOrder;

    private Integer enrollmentCount;

    private Integer favoriteCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}