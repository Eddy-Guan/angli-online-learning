package com.angli.online.mapper;

import com.angli.online.entity.Checkin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CheckinMapper extends BaseMapper<Checkin> {

    @Select("SELECT COUNT(*) FROM (" +
            "SELECT checkin_date, ROW_NUMBER() OVER (ORDER BY checkin_date DESC) AS rn " +
            "FROM checkins " +
            "WHERE user_id = #{userId} " +
            "ORDER BY checkin_date DESC " +
            "LIMIT 365" +
            ") t " +
            "WHERE checkin_date = DATE_SUB(CURDATE(), INTERVAL (rn - 1) DAY)")
    Integer getContinuousDays(@Param("userId") Long userId);

}