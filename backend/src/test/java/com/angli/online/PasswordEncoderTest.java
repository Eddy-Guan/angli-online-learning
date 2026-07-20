package com.angli.online;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode("123456");
        System.out.println("BCrypt hash for '123456': " + encoded);
        System.out.println("Hash length: " + encoded.length());
        
        boolean matches = encoder.matches("123456", encoded);
        System.out.println("Matches '123456': " + matches);
        
        String existingHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq";
        boolean existingMatches = encoder.matches("123456", existingHash);
        System.out.println("Existing hash matches '123456': " + existingMatches);
    }
}