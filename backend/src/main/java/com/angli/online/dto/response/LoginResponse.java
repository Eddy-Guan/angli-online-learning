package com.angli.online.dto.response;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private String role;

    private String realName;

    private Long userId;

}