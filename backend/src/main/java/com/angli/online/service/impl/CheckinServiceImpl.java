package com.angli.online.service.impl;

import com.angli.online.entity.Checkin;
import com.angli.online.mapper.CheckinMapper;
import com.angli.online.service.CheckinService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CheckinServiceImpl extends ServiceImpl<CheckinMapper, Checkin> implements CheckinService {

    @Override
    public boolean checkin(Long userId, Long studentId) {
        LocalDate today = LocalDate.now();
        
        Checkin existing = baseMapper.selectOne(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getUserId, userId)
                .eq(studentId != null, Checkin::getStudentId, studentId)
                .eq(Checkin::getCheckinDate, today));

        if (existing != null) {
            return false;
        }

        Checkin checkin = new Checkin();
        checkin.setUserId(userId);
        checkin.setStudentId(studentId);
        checkin.setCheckinDate(today);
        baseMapper.insert(checkin);
        return true;
    }

    @Override
    public int getContinuousDays(Long userId, Long studentId) {
        LocalDate today = LocalDate.now();
        int days = 0;
        
        for (int i = 0; i < 365; i++) {
            LocalDate checkDate = today.minusDays(i);
            Checkin checkin = baseMapper.selectOne(new LambdaQueryWrapper<Checkin>()
                    .eq(Checkin::getUserId, userId)
                    .eq(studentId != null, Checkin::getStudentId, studentId)
                    .eq(Checkin::getCheckinDate, checkDate));
            
            if (checkin != null) {
                days++;
            } else {
                break;
            }
        }
        
        return days;
    }

    @Override
    public boolean hasCheckedToday(Long userId, Long studentId) {
        LocalDate today = LocalDate.now();
        Checkin checkin = baseMapper.selectOne(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getUserId, userId)
                .eq(studentId != null, Checkin::getStudentId, studentId)
                .eq(Checkin::getCheckinDate, today));
        return checkin != null;
    }

}