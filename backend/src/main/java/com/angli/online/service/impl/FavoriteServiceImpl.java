package com.angli.online.service.impl;

import com.angli.online.entity.Course;
import com.angli.online.entity.Favorite;
import com.angli.online.mapper.FavoriteMapper;
import com.angli.online.service.FavoriteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Override
    public boolean addFavorite(Long userId, Long courseId) {
        Favorite existing = baseMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getCourseId, courseId));

        if (existing != null) {
            return false;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setCourseId(courseId);
        baseMapper.insert(favorite);
        return true;
    }

    @Override
    public boolean removeFavorite(Long userId, Long courseId) {
        int deleted = baseMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getCourseId, courseId));
        return deleted > 0;
    }

    @Override
    public boolean isFavorite(Long userId, Long courseId) {
        Favorite favorite = baseMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getCourseId, courseId));
        return favorite != null;
    }

    @Override
    public List<Favorite> getFavorites(Long userId) {
        return baseMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt));
    }

}