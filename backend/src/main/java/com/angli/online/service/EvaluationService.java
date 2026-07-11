package com.angli.online.service;

import com.angli.online.dto.request.EvaluationRequest;
import com.angli.online.entity.Evaluation;

import java.util.List;

public interface EvaluationService {

    Evaluation createEvaluation(Long userId, EvaluationRequest request);

    Evaluation getEvaluationById(Long id);

    List<Evaluation> getEvaluationsByCourseId(Long courseId);

    Double getCourseAverageRating(Long courseId);

}
