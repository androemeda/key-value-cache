package com.cache.controller;

import com.cache.model.CacheResponse;
import com.cache.model.PutRequest;
import com.cache.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;

@RestController
public class CacheController {

    private final CacheService cacheService;
    
    private static final CacheResponse SUCCESS_PUT_RESPONSE = createSuccessResponse();
    private static final CacheResponse ERROR_NOT_FOUND_RESPONSE = createNotFoundResponse();
    
    private final ConcurrentHashMap<String, CacheResponse> responseCache = new ConcurrentHashMap<>(1024);

    @Autowired
    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    private static CacheResponse createSuccessResponse() {
        CacheResponse response = new CacheResponse();
        response.setStatus("OK");
        response.setMessage("Key inserted/updated successfully.");
        return response;
    }
    
    private static CacheResponse createNotFoundResponse() {
        CacheResponse response = new CacheResponse();
        response.setStatus("ERROR");
        response.setMessage("Key not found.");
        return response;
    }

    @PostMapping("/put")
    public ResponseEntity<CacheResponse> put(@RequestBody PutRequest request) {
        try {
            if (request.getKey() == null || request.getValue() == null) {
                return ResponseEntity.badRequest()
                        .body(CacheResponse.error("Key and value cannot be null"));
            }

            boolean success = cacheService.put(request.getKey(), request.getValue());
            
            if (success) {
                return ResponseEntity.ok(SUCCESS_PUT_RESPONSE);
            } else {
                return ResponseEntity.badRequest()
                        .body(CacheResponse.error("Key or value exceeds maximum length of 256 characters"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(CacheResponse.error("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/get")
    public ResponseEntity<CacheResponse> get(@RequestParam String key) {
        try {
            String value = cacheService.get(key);
            
            if (value != null) {
                return ResponseEntity.ok(responseCache.computeIfAbsent(
                    key + ":" + value,
                    k -> {
                        CacheResponse response = new CacheResponse();
                        response.setStatus("OK");
                        response.setKey(key);
                        response.setValue(value);
                        return response;
                    }
                ));
            } else {
                return ResponseEntity.ok(ERROR_NOT_FOUND_RESPONSE);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(CacheResponse.error("Error: " + e.getMessage()));
        }
    }
}

/**
 * 
 * mvn clean package
 * docker build -t key-value-cache .
 * docker run -p 7171:7171 key-value-cache
 * locust -f locustfile.py --host=http://localhost:7171
 * 
 * 
 * mvn clean package
 * docker build -t key-value-cache:1.0.0 .
 * docker tag key-value-cache:1.0.0 yourusername/key-value-cache:1.0.0
 * docker login
 * docker push yourusername/key-value-cache:1.0.0
 * yourusername/key-value-cache:1.0.0
 * 
 */