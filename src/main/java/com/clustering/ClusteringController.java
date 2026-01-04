package com.clustering;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Configure for your domain in production
public class ClusteringController {

    @Autowired
    private ClusteringService clusteringService;

    @PostMapping("/cluster")
    public ResponseEntity<?> performClustering(@RequestBody ClusteringRequest request) {
        try {
            ClusteringResponse response = clusteringService.performClustering(
                request.getStudents(),
                request.getCategory(),
                request.getClusters()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "WEKA Clustering"));
    }
}

