package com.angli.online.controller;

import com.angli.online.dto.request.OrderCreateRequest;
import com.angli.online.dto.response.ApiResponse;
import com.angli.online.entity.Order;
import com.angli.online.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parent/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<Order> createOrder(@RequestParam Long userId, @Valid @RequestBody OrderCreateRequest request) {
        Order order = orderService.createOrder(userId, request);
        return ApiResponse.success(order);
    }

    @GetMapping("/{id}")
    public ApiResponse<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ApiResponse.success(order);
    }

    @GetMapping
    public ApiResponse<List<Order>> getOrdersByUserId(@RequestParam Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ApiResponse.success(orders);
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<Map<String, Object>> payOrder(@PathVariable Long id) {
        Order order = orderService.payOrder(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("order", order);
        
        return ApiResponse.success(result);
    }

    @PostMapping("/{id}/query")
    public ApiResponse<Order> queryOrderStatus(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ApiResponse.success(order);
    }
}
