package com.angli.online.service.impl;

import com.angli.online.dto.request.LoginRequest;
import com.angli.online.dto.request.RegisterRequest;
import com.angli.online.dto.response.LoginResponse;
import com.angli.online.entity.User;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.AuthService;
import com.angli.online.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone())
                .eq(User::getStatus, "ACTIVE"));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
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