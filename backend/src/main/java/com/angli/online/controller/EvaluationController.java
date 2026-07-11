package com.angli.online.controller;

import com.angli.online.dto.request.EvaluationRequest;
import com.angli.online.dto.response.ApiResponse;
import com.angli.online.entity.Evaluation;
import com.angli.online.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parent/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public ApiResponse<Evaluation> createEvaluation(@RequestParam Long userId, @Valid @RequestBody EvaluationRequest request) {
        Evaluation evaluation = evaluationService.createEvaluation(userId, request);
        return ApiResponse.success(evaluation);
    }

    @GetMapping("/{id}")
    public ApiResponse<Evaluation> getEvaluationById(@PathVariable Long id) {
        Evaluation evaluation = evaluationService.getEvaluationById(id);
        return ApiResponse.success(evaluation);
    }

    @GetMapping("/course/{courseId}")
    public ApiResponse<List<Evaluation>> getEvaluationsByCourseId(@PathVariable Long courseId) {
        List<Evaluation> evaluations = evaluationService.getEvaluationsByCourseId(courseId);
        return ApiResponse.success(evaluations);
    }

    @GetMapping("/course/{courseId}/rating")
    public ApiResponse<Double> getCourseAverageRating(@PathVariable Long courseId) {
        Double rating = evaluationService.getCourseAverageRating(courseId);
        return ApiResponse.success(rating);
    }

}
