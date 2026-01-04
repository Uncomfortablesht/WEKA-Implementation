package com.clustering;

import java.util.List;
import java.util.Map;

public class ClusteringRequest {
    private List<Map<String, Object>> students;
    private String category; // "all", "literacy", "math"
    private int clusters = 3;

    // Getters and setters
    public List<Map<String, Object>> getStudents() {
        return students;
    }

    public void setStudents(List<Map<String, Object>> students) {
        this.students = students;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getClusters() {
        return clusters;
    }

    public void setClusters(int clusters) {
        this.clusters = clusters;
    }
}

