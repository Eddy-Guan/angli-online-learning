package com.angli.online.service;

import java.util.Map;

public interface WxPayService {

    Map<String, String> createOrder(Long orderId, String openId);

    Map<String, String> parseNotify(String xmlData);

    boolean verifySignature(Map<String, String> params);

    String buildXmlResponse(boolean success, String message);
}
