package com.angli.online.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateRequest {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    private Long studentId;

}
