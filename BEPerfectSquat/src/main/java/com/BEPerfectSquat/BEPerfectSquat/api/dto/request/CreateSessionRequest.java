package com.BEPerfectSquat.BEPerfectSquat.api.dto.request;

import com.BEPerfectSquat.BEPerfectSquat.domain.enums.ExerciseType;

public class CreateSessionRequest {
    private ExerciseType exerciseType = ExerciseType.SQUAT;

    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(ExerciseType exerciseType) {
        this.exerciseType = exerciseType;
    }
}
