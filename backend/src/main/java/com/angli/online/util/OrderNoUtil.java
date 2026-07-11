package com.angli.online.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class OrderNoUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return timestamp + random;
    }

}