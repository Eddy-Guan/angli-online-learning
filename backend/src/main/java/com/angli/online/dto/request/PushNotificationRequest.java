package com.angli.online.dto.request;

import lombok.Data;

@Data
public class PushNotificationRequest {

    private Long courseId;

    private String summary;

    private String homework;

    private String goodStudents;

}