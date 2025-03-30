package com.cache.controller;

import com.cache.model.CacheResponse;
import com.cache.model.PutRequest;
import com.cache.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CacheController {

    private final CacheService cacheService;

    @Autowired
    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
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
                return ResponseEntity.ok(CacheResponse.success("Key inserted/updated successfully." , request.getKey() , request.getValue()));
            } else {
                return ResponseEntity.badRequest()
                        .body(CacheResponse.error("Key or value exceeds maximum length of 256 characters"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(CacheResponse.error("Error processing request: " + e.getMessage()));
        }
    }

    @GetMapping("/get")
    public ResponseEntity<CacheResponse> get(@RequestParam String key) {
        try {
            String value = cacheService.get(key);
            
            if (value != null) {
                return ResponseEntity.ok(CacheResponse.success(key, value));
            } else {
                return ResponseEntity.ok(CacheResponse.error("Key not found."));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(CacheResponse.error("Error processing request: " + e.getMessage()));
        }
    }
}