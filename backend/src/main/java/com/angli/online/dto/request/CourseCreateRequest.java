package com.angli.online.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseCreateRequest {

    @NotBlank(message = "课程名称不能为空")
    private String title;

    private String description;

    private String category;

    @NotNull(message = "课程价格不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull(message = "总课时不能为空")
    private Integer totalHours;

    private String coverImage;

}
