package com.clustering;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClusteringService {

    public ClusteringResponse performClustering(
            List<Map<String, Object>> students,
            String category,
            int numClusters) throws Exception {

        if (students == null || students.size() < numClusters) {
            throw new IllegalArgumentException("Need at least " + numClusters + " students");
        }

        // Create ARFF data structure
        Instances data = createInstances(students, category);

        // Remove class attribute if exists
        if (data.classIndex() == -1 && data.numAttributes() > 0) {
            data.setClassIndex(data.numAttributes() - 1);
        }
        Remove remove = new Remove();
        remove.setAttributeIndices("last");
        remove.setInputFormat(data);
        Instances dataClusterer = Filter.useFilter(data, remove);

        // Build K-Means clusterer
        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setNumClusters(numClusters);
        kmeans.setSeed(42);
        kmeans.setPreserveInstancesOrder(true);
        kmeans.buildClusterer(dataClusterer);

        // Get centroids for labeling
        Instances centroids = kmeans.getClusterCentroids();

        // Assign clusters
        List<ClusteringResponse.ClusterAssignment> assignments = new ArrayList<>();
        Map<Integer, List<Double>> clusterScores = new HashMap<>();

        for (int i = 0; i < dataClusterer.numInstances(); i++) {
            int cluster = kmeans.clusterInstance(dataClusterer.instance(i));
            Map<String, Object> student = students.get(i);

            // Calculate score
            double score = calculateScore(student, category);

            // Store for labeling
            clusterScores.computeIfAbsent(cluster, k -> new ArrayList<>()).add(score);

            ClusteringResponse.ClusterAssignment assignment = new ClusteringResponse.ClusterAssignment();
            assignment.setUserId(getUserId(student));
            assignment.setClusterNumber(cluster);
            assignment.setScore(score);
            assignments.add(assignment);
        }

        // Assign labels based on cluster averages
        Map<Integer, String> labelMap = assignLabels(clusterScores, numClusters);
        for (ClusteringResponse.ClusterAssignment assignment : assignments) {
            assignment.setClusterLabel(labelMap.get(assignment.getClusterNumber()));
        }

        // Generate report
        Map<String, Object> report = generateReport(assignments, numClusters);

        // Create response
        ClusteringResponse response = new ClusteringResponse();
        response.setSuccess(true);
        response.setMessage("Clustering completed successfully");
        response.setAlgorithm("WEKA K-Means");
        response.setAssignments(assignments);
        response.setReport(report);

        return response;
    }

    private Instances createInstances(List<Map<String, Object>> students, String category) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        if ("literacy".equals(category)) {
            attributes.add(new Attribute("literacy_score"));
            attributes.add(new Attribute("games_played"));
            attributes.add(new Attribute("total_score"));
        } else if ("math".equals(category)) {
            attributes.add(new Attribute("math_score"));
            attributes.add(new Attribute("games_played"));
            attributes.add(new Attribute("total_score"));
        } else {
            attributes.add(new Attribute("literacy_score"));
            attributes.add(new Attribute("math_score"));
            attributes.add(new Attribute("games_played"));
            attributes.add(new Attribute("total_score"));
        }

        Instances data = new Instances("student_performance", attributes, students.size());

        for (Map<String, Object> student : students) {
            double[] values = new double[attributes.size()];
            int idx = 0;

            if ("literacy".equals(category)) {
                values[idx++] = getDouble(student, "literacy_score");
                values[idx++] = getDouble(student, "games_played");
                values[idx++] = getDouble(student, "total_score");
            } else if ("math".equals(category)) {
                values[idx++] = getDouble(student, "math_score");
                values[idx++] = getDouble(student, "games_played");
                values[idx++] = getDouble(student, "total_score");
            } else {
                values[idx++] = getDouble(student, "literacy_score");
                values[idx++] = getDouble(student, "math_score");
                values[idx++] = getDouble(student, "games_played");
                values[idx++] = getDouble(student, "total_score");
            }

            data.add(new DenseInstance(1.0, values));
        }

        return data;
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int getUserId(Map<String, Object> student) {
        Object userId = student.get("user_id");
        if (userId instanceof Number) {
            return ((Number) userId).intValue();
        }
        try {
            return Integer.parseInt(userId.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private double calculateScore(Map<String, Object> student, String category) {
        if ("literacy".equals(category)) {
            return getDouble(student, "literacy_score");
        } else if ("math".equals(category)) {
            return getDouble(student, "math_score");
        } else {
            double literacy = getDouble(student, "literacy_score");
            double math = getDouble(student, "math_score");
            return (literacy + math) / 2.0;
        }
    }

    private Map<Integer, String> assignLabels(Map<Integer, List<Double>> clusterScores, int numClusters) {
        Map<Integer, String> labelMap = new HashMap<>();
        Map<Integer, Double> averages = new HashMap<>();

        // Calculate averages
        for (Map.Entry<Integer, List<Double>> entry : clusterScores.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            averages.put(entry.getKey(), avg);
        }

        // Sort by average score
        List<Map.Entry<Integer, Double>> sorted = new ArrayList<>(averages.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Assign labels
        String[] labels = {"High Achievers", "Average Performers", "Needs Support"};
        for (int i = 0; i < sorted.size() && i < labels.length; i++) {
            labelMap.put(sorted.get(i).getKey(), labels[i]);
        }

        // Fill remaining clusters
        for (int i = 0; i < numClusters; i++) {
            if (!labelMap.containsKey(i)) {
                labelMap.put(i, "Cluster " + i);
            }
        }

        return labelMap;
    }

    private Map<String, Object> generateReport(List<ClusteringResponse.ClusterAssignment> assignments, int numClusters) {
        Map<String, Object> report = new HashMap<>();
        report.put("analysis_date", new Date().toString());
        report.put("total_students", assignments.size());
        report.put("number_of_clusters", numClusters);

        // Count by cluster
        Map<Integer, Integer> counts = new HashMap<>();
        for (ClusteringResponse.ClusterAssignment assignment : assignments) {
            counts.put(assignment.getClusterNumber(), 
                counts.getOrDefault(assignment.getClusterNumber(), 0) + 1);
        }

        List<Map<String, Object>> clusters = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            Map<String, Object> clusterInfo = new HashMap<>();
            clusterInfo.put("cluster_number", entry.getKey());
            clusterInfo.put("student_count", entry.getValue());
            clusterInfo.put("percentage", 
                (entry.getValue() * 100.0) / assignments.size());
            
            // Find label
            String label = assignments.stream()
                .filter(a -> a.getClusterNumber() == entry.getKey())
                .findFirst()
                .map(ClusteringResponse.ClusterAssignment::getClusterLabel)
                .orElse("Cluster " + entry.getKey());
            clusterInfo.put("label", label);
            
            clusters.add(clusterInfo);
        }

        report.put("clusters", clusters);
        return report;
    }
}

