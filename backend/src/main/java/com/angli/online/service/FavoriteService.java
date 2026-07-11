package com.angli.online.service;

import com.angli.online.entity.Favorite;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface FavoriteService extends IService<Favorite> {

    boolean addFavorite(Long userId, Long courseId);

    boolean removeFavorite(Long userId, Long courseId);

    boolean isFavorite(Long userId, Long courseId);

    List<Favorite> getFavorites(Long userId);

}