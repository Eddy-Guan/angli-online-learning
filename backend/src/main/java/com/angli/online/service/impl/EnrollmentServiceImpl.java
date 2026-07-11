package com.angli.online.service.impl;

import com.angli.online.entity.Enrollment;
import com.angli.online.mapper.EnrollmentMapper;
import com.angli.online.service.EnrollmentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentServiceImpl extends ServiceImpl<EnrollmentMapper, Enrollment> implements EnrollmentService {

    @Override
    public Enrollment enroll(Long userId, Long courseId, Long studentId) {
        Enrollment existing = baseMapper.selectOne(new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getUserId, userId)
                .eq(Enrollment::getCourseId, courseId)
                .eq(studentId != null, Enrollment::getStudentId, studentId));

        if (existing != null) {
            return existing;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setStatus("ENROLLED");
        enrollment.setProgress(0);
        enrollment.setEnrolledAt(LocalDateTime.now());
        baseMapper.insert(enrollment);
        return enrollment;
    }

    @Override
    public List<Enrollment> getEnrollments(Long userId) {
        return baseMapper.selectList(new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getUserId, userId)
                .orderByDesc(Enrollment::getEnrolledAt));
    }

    @Override
    public Enrollment getEnrollment(Long userId, Long courseId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getUserId, userId)
                .eq(Enrollment::getCourseId, courseId));
    }

}