package com.sysrec.projet_ds1_java.Model;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RessourceModel {
    private final IntegerProperty resourceId = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty difficulty = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final StringProperty keywords = new SimpleStringProperty();
    private final IntegerProperty teacherId = new SimpleIntegerProperty();
    private final BooleanProperty isApproved = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final StringProperty format = new SimpleStringProperty();
    private final StringProperty tags = new SimpleStringProperty();
    private final StringProperty url = new SimpleStringProperty();
    private final DoubleProperty averageRating = new SimpleDoubleProperty();

    public RessourceModel(int resourceId, String title, String description, String difficulty,
                          String category, String keywords, int teacherId, boolean isApproved,
                          LocalDateTime createdAt, String format, String tags, String url) {
        this.resourceId.set(resourceId);
        this.title.set(title);
        this.description.set(description);
        this.difficulty.set(difficulty);
        this.category.set(category);
        this.keywords.set(keywords);
        this.teacherId.set(teacherId);
        this.isApproved.set(isApproved);
        this.createdAt.set(createdAt);
        this.format.set(format);
        this.tags.set(tags);
        this.url.set(url);
    }

    public RessourceModel() {}

    public String afficheRessource() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "ID: " + getResourceId() +
                "\nTitre: " + getTitle() +
                "\nDescription: " + getDescription() +
                "\nDifficulté: " + getDifficulty() +
                "\nCatégorie: " + getCategory() +
                "\nFormat: " + getFormat() +
                "\nMots-clés: " + getKeywords() +
                "\nTags: " + getTags() +
                "\nLien: " + getUrl() +
                "\nID Enseignant: " + getTeacherId() +
                "\nApprouvée: " + (isApproved() ? "Oui" : "Non") +
                "\nCréée le: " + (getCreatedAt() != null ? getCreatedAt().format(formatter) : "Non spécifiée") +
                "\nNote Moyenne: " + getAverageRating();
    }

    // Property getters
    public StringProperty titleProperty() { return title; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty difficultyProperty() { return difficulty; }
    public DoubleProperty averageRatingProperty() { return averageRating; }

    // Getters
    public int getResourceId() { return resourceId.get(); }
    public String getTitle() { return title.get(); }
    public String getDescription() { return description.get(); }
    public String getDifficulty() { return difficulty.get(); }
    public String getCategory() { return category.get(); }
    public String getKeywords() { return keywords.get(); }
    public String getFormat() { return format.get(); }
    public String getTags() { return tags.get(); }
    public String getUrl() { return url.get(); }
    public int getTeacherId() { return teacherId.get(); }
    public boolean isApproved() { return isApproved.get(); }
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public double getAverageRating() { return averageRating.get(); }

    // Setters
    public void setResourceId(int resourceId) { this.resourceId.set(resourceId); }
    public void setTitle(String title) { this.title.set(title); }
    public void setDescription(String description) { this.description.set(description); }
    public void setDifficulty(String difficulty) { this.difficulty.set(difficulty); }
    public void setCategory(String category) { this.category.set(category); }
    public void setKeywords(String keywords) { this.keywords.set(keywords); }
    public void setFormat(String format) { this.format.set(format); }
    public void setTags(String tags) { this.tags.set(tags); }
    public void setUrl(String url) { this.url.set(url); }
    public void setTeacherId(int teacherId) { this.teacherId.set(teacherId); }
    public void setApproved(boolean approved) { this.isApproved.set(approved); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public void setAverageRating(double averageRating) { this.averageRating.set(averageRating); }
}
