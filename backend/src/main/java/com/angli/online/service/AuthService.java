package com.angli.online.service;

import com.angli.online.dto.request.LoginRequest;
import com.angli.online.dto.request.RegisterRequest;
import com.angli.online.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

}