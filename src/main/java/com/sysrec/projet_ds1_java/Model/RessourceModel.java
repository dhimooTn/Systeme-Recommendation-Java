package com.sysrec.projet_ds1_java.Model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Model class representing a resource in the system.
 */
public class RessourceModel {
    private final IntegerProperty resourceId = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty difficulty = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final StringProperty keywords = new SimpleStringProperty();
    private final IntegerProperty teacherId = new SimpleIntegerProperty();
    private final BooleanProperty isApproved = new SimpleBooleanProperty();
    private final BooleanProperty isPrivate = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final DoubleProperty averageRating = new SimpleDoubleProperty();
    private final IntegerProperty studentCount = new SimpleIntegerProperty();

    public RessourceModel(int resourceId, String title, String description, String difficulty,
                          String category, String keywords, int teacherId, boolean isApproved,
                          boolean isPrivate, LocalDateTime createdAt, int studentCount, double averageRating) {
        this.resourceId.set(resourceId);
        this.title.set(title);
        this.description.set(description);
        this.difficulty.set(difficulty);
        this.category.set(category);
        this.keywords.set(keywords);
        this.teacherId.set(teacherId);
        this.isApproved.set(isApproved);
        this.isPrivate.set(isPrivate);
        this.createdAt.set(createdAt);
        this.studentCount.set(studentCount);
    }

    // Property getters
    public IntegerProperty resourceIdProperty() { return resourceId; }
    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty difficultyProperty() { return difficulty; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty keywordsProperty() { return keywords; }
    public IntegerProperty teacherIdProperty() { return teacherId; }
    public BooleanProperty isApprovedProperty() { return isApproved; }
    public BooleanProperty isPrivateProperty() { return isPrivate; }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public DoubleProperty averageRatingProperty() { return averageRating; }
    public IntegerProperty studentCountProperty() { return studentCount; }

    // Getters
    public int getResourceId() { return resourceId.get(); }
    public String getTitle() { return title.get(); }
    public String getDescription() { return description.get(); }
    public String getDifficulty() { return difficulty.get(); }
    public String getCategory() { return category.get(); }
    public String getKeywords() { return keywords.get(); }
    public int getTeacherId() { return teacherId.get(); }
    public boolean isApproved() { return isApproved.get(); }
    public boolean isPrivate() { return isPrivate.get(); }
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public double getAverageRating() { return averageRating.get(); }
    public int getStudentCount() { return studentCount.get(); }

    // Setters
    public void setResourceId(int resourceId) { this.resourceId.set(resourceId); }
    public void setTitle(String title) { this.title.set(title); }
    public void setDescription(String description) { this.description.set(description); }
    public void setDifficulty(String difficulty) { this.difficulty.set(difficulty); }
    public void setCategory(String category) { this.category.set(category); }
    public void setKeywords(String keywords) { this.keywords.set(keywords); }
    public void setTeacherId(int teacherId) { this.teacherId.set(teacherId); }
    public void setApproved(boolean approved) { this.isApproved.set(approved); }
    public void setPrivate(boolean isPrivate) { this.isPrivate.set(isPrivate); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public void setAverageRating(double averageRating) { this.averageRating.set(averageRating); }
    public void setStudentCount(int studentCount) { this.studentCount.set(studentCount); }
}