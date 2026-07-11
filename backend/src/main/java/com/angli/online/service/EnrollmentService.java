package com.angli.online.service;

import com.angli.online.entity.Enrollment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface EnrollmentService extends IService<Enrollment> {

    Enrollment enroll(Long userId, Long courseId, Long studentId);

    List<Enrollment> getEnrollments(Long userId);

    Enrollment getEnrollment(Long userId, Long courseId);

}