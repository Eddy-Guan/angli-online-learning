package com.angli.online.service;

import com.angli.online.dto.request.OrderCreateRequest;
import com.angli.online.entity.Order;

import java.util.List;

public interface OrderService {

    Order createOrder(Long userId, OrderCreateRequest request);

    Order getOrderById(Long id);

    Order getOrderByOrderNo(String orderNo);

    List<Order> getOrdersByUserId(Long userId);

    Order payOrder(Long orderId);

    void updateOrder(Order order);

    void enrollCourse(Long orderId);

}
