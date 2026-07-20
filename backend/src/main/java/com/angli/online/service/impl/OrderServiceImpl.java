package com.angli.online.service.impl;

import com.angli.online.dto.request.OrderCreateRequest;
import com.angli.online.entity.Course;
import com.angli.online.entity.Enrollment;
import com.angli.online.entity.Order;
import com.angli.online.entity.User;
import com.angli.online.mapper.CourseMapper;
import com.angli.online.mapper.EnrollmentMapper;
import com.angli.online.mapper.OrderMapper;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.OrderService;
import com.angli.online.util.OrderNoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final EnrollmentMapper enrollmentMapper;

    public OrderServiceImpl(OrderMapper orderMapper, CourseMapper courseMapper, 
                           UserMapper userMapper, EnrollmentMapper enrollmentMapper) {
        this.orderMapper = orderMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.enrollmentMapper = enrollmentMapper;
    }

    @Override
    @Transactional
    public Order createOrder(Long userId, OrderCreateRequest request) {
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        if (!"PUBLISHED".equals(course.getStatus())) {
            throw new RuntimeException("课程未上架");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Order existingOrder = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getCourseId, request.getCourseId())
                .eq(Order::getStatus, "PAID"));
        if (existingOrder != null) {
            throw new RuntimeException("您已购买该课程");
        }

        Order order = new Order();
        order.setOrderNo(OrderNoUtil.generateOrderNo());
        order.setUserId(userId);
        order.setCourseId(request.getCourseId());
        order.setStudentId(request.getStudentId());
        order.setAmount(course.getPrice());
        order.setStatus("PENDING");

        orderMapper.insert(order);
        return order;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        return orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt));
    }

    @Override
    @Transactional
    public Order payOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许支付");
        }

        order.setStatus("PAID");
        orderMapper.updateById(order);

        enrollCourse(orderId);

        return order;
    }

    @Override
    public void updateOrder(Order order) {
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void enrollCourse(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return;
        }

        Enrollment existingEnrollment = enrollmentMapper.selectOne(new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getUserId, order.getUserId())
                .eq(Enrollment::getCourseId, order.getCourseId()));
        if (existingEnrollment == null) {
            Enrollment enrollment = new Enrollment();
            enrollment.setUserId(order.getUserId());
            enrollment.setCourseId(order.getCourseId());
            enrollment.setStudentId(order.getStudentId());
            enrollment.setStatus("ENROLLED");
            enrollment.setProgress(0);
            enrollmentMapper.insert(enrollment);
        }
    }

}
