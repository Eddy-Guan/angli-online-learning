package com.angli.online.service;

import com.angli.online.entity.Checkin;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CheckinService extends IService<Checkin> {

    boolean checkin(Long userId, Long studentId);

    int getContinuousDays(Long userId, Long studentId);

    boolean hasCheckedToday(Long userId, Long studentId);

}