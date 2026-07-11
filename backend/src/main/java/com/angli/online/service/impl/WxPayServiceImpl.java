package com.angli.online.service.impl;

import com.angli.online.config.WxPayConfig;
import com.angli.online.entity.Order;
import com.angli.online.mapper.OrderMapper;
import com.angli.online.service.WxPayService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.*;

@Service
public class WxPayServiceImpl implements WxPayService {

    private final WxPayConfig wxPayConfig;
    private final OrderMapper orderMapper;

    public WxPayServiceImpl(WxPayConfig wxPayConfig, OrderMapper orderMapper) {
        this.wxPayConfig = wxPayConfig;
        this.orderMapper = orderMapper;
    }

    @Override
    public Map<String, String> createOrder(Long orderId, String openId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("订单状态异常");
        }

        Map<String, String> params = new HashMap<>();
        params.put("appid", wxPayConfig.getAppId());
        params.put("mch_id", wxPayConfig.getMchId());
        params.put("nonce_str", generateNonceStr());
        params.put("body", "昂立在线课程-" + order.getOrderNo());
        params.put("out_trade_no", order.getOrderNo());
        params.put("total_fee", String.valueOf(order.getAmount().multiply(new java.math.BigDecimal("100")).intValue()));
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("notify_url", wxPayConfig.getNotifyUrl());
        params.put("trade_type", wxPayConfig.getTradeType());
        params.put("openid", openId);
        params.put("sign", generateSignature(params));

        return params;
    }

    @Override
    public Map<String, String> parseNotify(String xmlData) {
        try {
            Map<String, String> result = new HashMap<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlData.getBytes("UTF-8"));
            org.w3c.dom.Document doc = builder.parse(inputStream);

            org.w3c.dom.NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    result.put(node.getNodeName(), node.getTextContent());
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("解析支付回调失败", e);
        }
    }

    @Override
    public boolean verifySignature(Map<String, String> params) {
        String sign = params.remove("sign");
        String calculatedSign = generateSignature(params);
        return calculatedSign.equalsIgnoreCase(sign);
    }

    @Override
    public String buildXmlResponse(boolean success, String message) {
        try {
            org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            org.w3c.dom.Element root = doc.createElement("xml");
            
            org.w3c.dom.Element returnCode = doc.createElement("return_code");
            returnCode.setTextContent(success ? "SUCCESS" : "FAIL");
            root.appendChild(returnCode);
            
            org.w3c.dom.Element returnMsg = doc.createElement("return_msg");
            returnMsg.setTextContent(message);
            root.appendChild(returnMsg);
            
            doc.appendChild(root);
            
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            return "<xml><return_code>FAIL</return_code><return_msg>构建响应失败</return_msg></xml>";
        }
    }

    private String generateNonceStr() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    }

    private String generateSignature(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (value != null && !value.isEmpty() && !"sign".equals(key)) {
                sb.append(key).append("=").append(value).append("&");
            }
        }
        sb.append("key=").append(wxPayConfig.getMchKey());
        
        return md5(sb.toString()).toUpperCase();
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sb.append("0");
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}
