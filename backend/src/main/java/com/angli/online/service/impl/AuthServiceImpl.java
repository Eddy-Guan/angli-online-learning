package com.angli.online.service.impl;

import com.angli.online.dto.request.LoginRequest;
import com.angli.online.dto.request.RegisterRequest;
import com.angli.online.dto.response.LoginResponse;
import com.angli.online.entity.User;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.AuthService;
import com.angli.online.service.SmsService;
import com.angli.online.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final SmsService smsService;

  public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
      SmsService smsService) {
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.smsService = smsService;
  }

  @Override
  public LoginResponse login(LoginRequest request) {
    User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
        .eq(User::getPhone, request.getPhone()));

    if (user == null) {
      log.warn("Login failed: user not found for phone {}", request.getPhone());
      throw new RuntimeException("手机号或密码错误");
    }

    if ("PENDING".equals(user.getStatus())) {
      log.warn("Login failed: user pending approval for phone {}", request.getPhone());
      throw new RuntimeException("您的账号正在审核中，请等待管理员审核通过");
    }

    if ("DISABLED".equals(user.getStatus())) {
      log.warn("Login failed: user disabled for phone {}", request.getPhone());
      throw new RuntimeException("您的账号已被禁用");
    }

    log.debug("Login attempt: phone={}, storedPasswordLength={}, inputPasswordLength={}",
        request.getPhone(), user.getPassword().length(), request.getPassword().length());

    boolean passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
    log.debug("Password match result: {}", passwordMatch);

    if (!passwordMatch) {
      log.warn("Login failed: password mismatch for phone {}", request.getPhone());
      throw new RuntimeException("手机号或密码错误");
    }

    Map<String, Object> claims = new HashMap<>();
    claims.put("phone", user.getPhone());
    claims.put("realName", user.getRealName());

    String token = jwtUtil.generateToken(user.getId(), user.getRole(), claims);

    LoginResponse response = new LoginResponse();
    response.setToken(token);
    response.setRole(user.getRole());
    response.setRealName(user.getRealName());
    response.setUserId(user.getId());

    return response;
  }

  @Override
  public void register(RegisterRequest request) {
    if (!smsService.verifyCode(request.getPhone(), request.getCode())) {
      throw new RuntimeException("验证码错误或已过期");
    }

    User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
        .eq(User::getPhone, request.getPhone()));

    if (existing != null) {
      throw new RuntimeException("该手机号已注册");
    }

    User user = new User();
    user.setPhone(request.getPhone());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRealName(request.getRealName());
    user.setRole(request.getRole().toUpperCase());

    if ("TEACHER".equals(user.getRole())) {
      user.setStatus("PENDING");
    } else {
      user.setStatus("ACTIVE");
    }

    userMapper.insert(user);
  }

}