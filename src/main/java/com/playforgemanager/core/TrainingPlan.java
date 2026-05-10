package com.playforgemanager.core;

public interface TrainingPlan {

    // Returns the main focus area of this training plan.
    String getFocus();

    // Returns the intensity level of this training plan.
    int getIntensity();
}