package com.clustering;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.EuclideanDistance;
import weka.core.Instance;
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

        // Calculate silhouette score
        double silhouetteScore = calculateSilhouetteScore(dataClusterer, kmeans, assignments);

        // Generate report
        Map<String, Object> report = generateReport(assignments, numClusters, silhouetteScore);

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

        // Sort by average score (highest to lowest)
        List<Map.Entry<Integer, Double>> sorted = new ArrayList<>(averages.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Define performance level labels
        String[] performanceLabels = {
            "High Achievers",           // Top performers
            "Above Average",            // Second tier
            "Average Performers",       // Middle tier
            "Below Average",            // Fourth tier
            "Needs Support"             // Lowest performers
        };
        
        // Assign meaningful labels based on performance ranking
        for (int i = 0; i < sorted.size(); i++) {
            int clusterNum = sorted.get(i).getKey();
            double avgScore = sorted.get(i).getValue();
            
            if (i < performanceLabels.length) {
                // Use predefined performance labels for top clusters
                labelMap.put(clusterNum, performanceLabels[i]);
            } else {
                // For clusters beyond the predefined labels, use performance-based naming
                if (avgScore >= 80) {
                    labelMap.put(clusterNum, "High Performers");
                } else if (avgScore >= 60) {
                    labelMap.put(clusterNum, "Average Performers");
                } else if (avgScore >= 40) {
                    labelMap.put(clusterNum, "Below Average");
                } else {
                    labelMap.put(clusterNum, "Needs Support");
                }
            }
        }

        // Ensure all clusters have labels (fallback for any missing)
        for (int i = 0; i < numClusters; i++) {
            if (!labelMap.containsKey(i)) {
                labelMap.put(i, "Average Performers");
            }
        }

        return labelMap;
    }

    /**
     * Calculate silhouette score for clustering quality assessment
     * Silhouette score ranges from -1 to 1:
     * - 1: Perfect clustering
     * - 0: Overlapping clusters
     * - -1: Wrong clustering
     */
    private double calculateSilhouetteScore(
            Instances data,
            SimpleKMeans clusterer,
            List<ClusteringResponse.ClusterAssignment> assignments) {
        
        if (data.numInstances() < 2) {
            return 0.0; // Cannot calculate with less than 2 instances
        }

        try {
            // Initialize distance function
            EuclideanDistance distance = new EuclideanDistance(data);
            distance.setDontNormalize(false);

            // Create cluster assignment map for quick lookup
            Map<Integer, Integer> instanceToCluster = new HashMap<>();
            for (int i = 0; i < assignments.size(); i++) {
                instanceToCluster.put(i, assignments.get(i).getClusterNumber());
            }

            // Group instances by cluster
            Map<Integer, List<Integer>> clusterInstances = new HashMap<>();
            for (int i = 0; i < data.numInstances(); i++) {
                int cluster = instanceToCluster.get(i);
                clusterInstances.computeIfAbsent(cluster, k -> new ArrayList<>()).add(i);
            }

            double totalSilhouette = 0.0;
            int validInstances = 0;

            // Calculate silhouette for each instance
            for (int i = 0; i < data.numInstances(); i++) {
                Instance instance = data.instance(i);
                int ownCluster = instanceToCluster.get(i);
                List<Integer> ownClusterInstances = clusterInstances.get(ownCluster);

                // Calculate a(i): average distance to other points in same cluster
                double a_i = 0.0;
                if (ownClusterInstances.size() > 1) {
                    double sumDist = 0.0;
                    int count = 0;
                    for (int j : ownClusterInstances) {
                        if (i != j) {
                            sumDist += distance.distance(instance, data.instance(j));
                            count++;
                        }
                    }
                    a_i = count > 0 ? sumDist / count : 0.0;
                } else {
                    a_i = 0.0; // Only one instance in cluster
                }

                // Calculate b(i): average distance to points in nearest other cluster
                double b_i = Double.MAX_VALUE;
                for (Map.Entry<Integer, List<Integer>> entry : clusterInstances.entrySet()) {
                    if (entry.getKey() != ownCluster) {
                        double avgDist = 0.0;
                        int count = 0;
                        for (int j : entry.getValue()) {
                            avgDist += distance.distance(instance, data.instance(j));
                            count++;
                        }
                        if (count > 0) {
                            avgDist = avgDist / count;
                            b_i = Math.min(b_i, avgDist);
                        }
                    }
                }

                // If only one cluster exists, set b_i to a large value
                if (b_i == Double.MAX_VALUE) {
                    b_i = a_i > 0 ? a_i * 2 : 1.0;
                }

                // Calculate silhouette for this instance: s(i) = (b(i) - a(i)) / max(a(i), b(i))
                double maxAB = Math.max(a_i, b_i);
                if (maxAB > 0) {
                    double silhouette_i = (b_i - a_i) / maxAB;
                    totalSilhouette += silhouette_i;
                    validInstances++;
                }
            }

            // Return average silhouette score
            return validInstances > 0 ? totalSilhouette / validInstances : 0.0;

        } catch (Exception e) {
            // If calculation fails, return 0.0
            System.err.println("Error calculating silhouette score: " + e.getMessage());
            return 0.0;
        }
    }

    private Map<String, Object> generateReport(
            List<ClusteringResponse.ClusterAssignment> assignments, 
            int numClusters,
            double silhouetteScore) {
        Map<String, Object> report = new HashMap<>();
        report.put("analysis_date", new Date().toString());
        report.put("total_students", assignments.size());
        report.put("number_of_clusters", numClusters);
        report.put("silhouette_score", Math.round(silhouetteScore * 10000.0) / 10000.0); // Round to 4 decimal places

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

