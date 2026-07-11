package com.angli.online.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvaluationRequest {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    private Long studentId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "最低评分为1星")
    @Max(value = 5, message = "最高评分为5星")
    private Integer rating;

    private String comment;

}
