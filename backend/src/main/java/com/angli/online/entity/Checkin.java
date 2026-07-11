package com.angli.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("checkins")
public class Checkin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long studentId;

    private LocalDate checkinDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}