package com.cache.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    
    private static final int MAX_STRING_LENGTH = 256;
    
    private static final int SEGMENT_COUNT = 16;
    private final ConcurrentHashMap<String, String>[] segments;
    
    private final AtomicInteger cacheSize = new AtomicInteger(0);
    
    public static final String CACHE_FULL_MESSAGE = "Cache is full";
    
    @SuppressWarnings("unchecked")
    public CacheService() {
        // Initialize sharded segments
        segments = new ConcurrentHashMap[SEGMENT_COUNT];
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            segments[i] = new ConcurrentHashMap<>(131072, 0.75f, 8);
        }
        
        logger.info("Cache service initialized with {} segments", SEGMENT_COUNT);
    }

    @PostConstruct
    public void init() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        logger.info("Maximum memory: {} MB", maxMemory);
    }
    
    private int getSegmentIndex(String key) {
        return Math.abs(key.hashCode() % SEGMENT_COUNT);
    }
    
    public boolean put(String key, String value) {
        if (key == null || value == null) return false;
        if (key.length() > MAX_STRING_LENGTH || value.length() > MAX_STRING_LENGTH) return false;
        
        int segmentIndex = getSegmentIndex(key);
        ConcurrentHashMap<String, String> segment = segments[segmentIndex];
        
        boolean isNewKey = !segment.containsKey(key);
        
        segment.put(key, value);
        
        if (isNewKey) {
            cacheSize.incrementAndGet();
        }
        
        return true;
    }
    
    public String get(String key) {
        if (key == null || key.length() > MAX_STRING_LENGTH) return null;
        
        int segmentIndex = getSegmentIndex(key);
        return segments[segmentIndex].get(key);
    }
    
    public int size() {
        return cacheSize.get();
    }
    
    public void clear() {
        for (ConcurrentHashMap<String, String> segment : segments) {
            segment.clear();
        }
        cacheSize.set(0);
    }
}