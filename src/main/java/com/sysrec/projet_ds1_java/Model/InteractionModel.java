package com.sysrec.projet_ds1_java.Model;

import java.time.LocalDateTime;

/**
 * Model class representing an interaction between a student and a resource.
 */
public class InteractionModel {
    private int interactionId;
    private int studentId;
    private int resourceId;
    private LocalDateTime savedAt;
    private int rating; // 1-5
    private LocalDateTime ratedAt;

    public InteractionModel(int interactionId, int studentId, int resourceId,
                            LocalDateTime savedAt, int rating, LocalDateTime ratedAt) {
        this.interactionId = interactionId;
        this.studentId = studentId;
        this.resourceId = resourceId;
        this.savedAt = savedAt;
        this.rating = rating;
        this.ratedAt = ratedAt;
    }

    public InteractionModel(int studentId, int resourceId, int rating) {
        this(0, studentId, resourceId, null, rating, null);
    }

    // Add a new constructor that matches the pattern used in StudentController.java
    public InteractionModel(int studentId, int resourceId, LocalDateTime savedAt, int rating, LocalDateTime ratedAt) {
        this(0, studentId, resourceId, savedAt, rating, ratedAt);
    }

    // Getters
    public int getInteractionId() { return interactionId; }
    public int getStudentId() { return studentId; }
    public int getResourceId() { return resourceId; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public int getRating() { return rating; }
    public LocalDateTime getRatedAt() { return ratedAt; }

    // Setters
    public void setInteractionId(int interactionId) { this.interactionId = interactionId; }
    public void setRating(int rating) { this.rating = rating; }
    public void setRatedAt(LocalDateTime ratedAt) { this.ratedAt = ratedAt; }
}