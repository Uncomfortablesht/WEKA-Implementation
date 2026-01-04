package com.clustering;

import java.util.List;
import java.util.Map;

public class ClusteringResponse {
    private boolean success;
    private String message;
    private String algorithm;
    private List<ClusterAssignment> assignments;
    private Map<String, Object> report;

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public List<ClusterAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<ClusterAssignment> assignments) {
        this.assignments = assignments;
    }

    public Map<String, Object> getReport() {
        return report;
    }

    public void setReport(Map<String, Object> report) {
        this.report = report;
    }

    public static class ClusterAssignment {
        private int userId;
        private int clusterNumber;
        private String clusterLabel;
        private double score;

        // Getters and setters
        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getClusterNumber() {
            return clusterNumber;
        }

        public void setClusterNumber(int clusterNumber) {
            this.clusterNumber = clusterNumber;
        }

        public String getClusterLabel() {
            return clusterLabel;
        }

        public void setClusterLabel(String clusterLabel) {
            this.clusterLabel = clusterLabel;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }
}

