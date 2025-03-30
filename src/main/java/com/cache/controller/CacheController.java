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
    
    // Pre-create common responses to avoid object creation during requests
    private static final CacheResponse KEY_NOT_FOUND_RESPONSE = CacheResponse.error("Key not found.");
    private static final CacheResponse SUCCESS_PUT_RESPONSE = CacheResponse.success("Key inserted/updated successfully.");
    private static final CacheResponse INVALID_KEY_VALUE_RESPONSE = CacheResponse.error("Key or value exceeds maximum length of 256 characters");

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
                return ResponseEntity.ok(SUCCESS_PUT_RESPONSE);
            } else {
                return ResponseEntity.badRequest()
                        .body(INVALID_KEY_VALUE_RESPONSE);
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
                return ResponseEntity.ok(KEY_NOT_FOUND_RESPONSE);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(CacheResponse.error("Error processing request: " + e.getMessage()));
        }
    }
}