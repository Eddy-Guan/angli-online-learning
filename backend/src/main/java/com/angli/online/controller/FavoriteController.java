package com.angli.online.controller;

import com.angli.online.dto.response.ApiResponse;
import com.angli.online.entity.Favorite;
import com.angli.online.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parent/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> addFavorite(@RequestParam Long userId, @RequestParam Long courseId) {
        boolean success = favoriteService.addFavorite(userId, courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ApiResponse.success(result);
    }

    @DeleteMapping
    public ApiResponse<Map<String, Object>> removeFavorite(@RequestParam Long userId, @RequestParam Long courseId) {
        boolean success = favoriteService.removeFavorite(userId, courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ApiResponse.success(result);
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> isFavorite(@RequestParam Long userId, @RequestParam Long courseId) {
        boolean isFavorite = favoriteService.isFavorite(userId, courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("isFavorite", isFavorite);
        return ApiResponse.success(result);
    }

    @GetMapping
    public ApiResponse<List<Favorite>> getFavorites(@RequestParam Long userId) {
        List<Favorite> favorites = favoriteService.getFavorites(userId);
        return ApiResponse.success(favorites);
    }

}