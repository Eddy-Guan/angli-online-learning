package com.angli.online.controller;

import com.angli.online.dto.request.LoginRequest;
import com.angli.online.dto.request.RegisterRequest;
import com.angli.online.dto.response.ApiResponse;
import com.angli.online.dto.response.LoginResponse;
import com.angli.online.service.AuthService;
import com.angli.online.service.SmsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final SmsService smsService;

  public AuthController(AuthService authService, SmsService smsService) {
    this.authService = authService;
    this.smsService = smsService;
  }

  @PostMapping("/send-code")
  public ApiResponse<Void> sendCode(@RequestBody Map<String, String> request) {
    String phone = request.get("phone");
    smsService.sendCode(phone);
    return ApiResponse.success("验证码已发送");
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return ApiResponse.success(response);
  }

  @PostMapping("/register")
  public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
    authService.register(request);
    return ApiResponse.success("注册成功");
  }

}