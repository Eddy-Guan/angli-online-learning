package com.angli.online.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseResponse {

    private Long id;

    private String title;

    private String description;

    private String coverImage;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String level;

    private Integer duration;

    private Integer lessons;

    private String teacherName;

    private String teacherAvatar;

    private String status;

    private String category;

    private Double rating;

    private Integer students;

    private String createdAt;

}