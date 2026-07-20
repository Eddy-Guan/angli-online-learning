package com.angli.online.service.impl;

import com.angli.online.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);
    
    private final Map<String, CodeInfo> codeStore = new ConcurrentHashMap<>();
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 5;

    private static class CodeInfo {
        String code;
        LocalDateTime expireTime;
        int sendCount;
    }

    @Override
    public void sendCode(String phone) {
        CodeInfo existing = codeStore.get(phone);
        
        if (existing != null && existing.sendCount >= 5) {
            throw new RuntimeException("发送次数过多，请稍后再试");
        }

        String code = generateCode();
        
        CodeInfo info = new CodeInfo();
        info.code = code;
        info.expireTime = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        info.sendCount = existing != null ? existing.sendCount + 1 : 1;
        
        codeStore.put(phone, info);
        
        log.info("发送验证码: phone={}, code={}, expireTime={}", phone, code, info.expireTime);
        
        System.out.println("===== 短信验证码 =====");
        System.out.println("手机号: " + phone);
        System.out.println("验证码: " + code);
        System.out.println("有效期: " + EXPIRATION_MINUTES + "分钟");
        System.out.println("=====================");
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        CodeInfo info = codeStore.get(phone);
        
        if (info == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(info.expireTime)) {
            codeStore.remove(phone);
            return false;
        }
        
        boolean valid = info.code.equals(code);
        
        if (valid) {
            codeStore.remove(phone);
        }
        
        return valid;
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
