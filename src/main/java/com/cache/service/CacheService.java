package com.cache.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    
    // Maximum length for keys and values as per requirements
    private static final int MAX_STRING_LENGTH = 256;
    
    // Primary cache storage - using ConcurrentHashMap for high concurrency
    private final ConcurrentHashMap<String, String> cache;
    
    // Access frequency tracking for eviction
    private final ConcurrentHashMap<String, AtomicInteger> accessFrequency;
    
    // Background scheduler for cache maintenance
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    // Memory monitoring
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    
    @Value("${cache.memory.threshold:70}")
    private int memoryThresholdPercentage;
    
    @Value("${cache.max.entries:1000000}")
    private int maxEntries;
    
    @Value("${cache.check.interval.seconds:5}")
    private int cacheCheckIntervalSeconds;
    
    @Value("${cache.initial.capacity:100000}")
    private int initialCapacity;
    
    @Value("${cache.load.factor:0.75}")
    private float loadFactor;
    
    @Value("${cache.concurrency.level:64}")
    private int concurrencyLevel;

    public CacheService() {
        // Initialize with optimal parameters for high throughput
        cache = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
        accessFrequency = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }
    
    @PostConstruct
    public void init() {
        // Start a background thread to periodically check memory and manage evictions
        scheduler.scheduleAtFixedRate(this::checkAndManageMemoryUsage, 
                cacheCheckIntervalSeconds, cacheCheckIntervalSeconds, TimeUnit.SECONDS);
        logger.info("Cache service initialized with max entries: {}, memory threshold: {}%, check interval: {} seconds",
                maxEntries, memoryThresholdPercentage, cacheCheckIntervalSeconds);
    }
    
    /**
     * Put a key-value pair in the cache
     * @param key the key
     * @param value the value
     * @return true if the operation was successful
     */
    public boolean put(String key, String value) {
        // Validate key and value
        if (key == null || value == null || 
            key.length() > MAX_STRING_LENGTH || value.length() > MAX_STRING_LENGTH) {
            return false;
        }
        
        // Check if we need to evict entries due to size constraints
        if (cache.size() >= maxEntries) {
            evictLeastFrequentlyUsed(maxEntries / 10); // Evict 10% of entries
        }
        
        // Store the value and reset access frequency
        cache.put(key, value);
        accessFrequency.put(key, new AtomicInteger(1));
        
        return true;
    }

    /**
     * Get a value by key from the cache
     * @param key the key
     * @return the value or null if key not found
     */
    public String get(String key) {
        // Validate key
        if (key == null || key.length() > MAX_STRING_LENGTH) {
            return null;
        }
        
        // Increment access frequency if the key exists
        AtomicInteger frequency = accessFrequency.get(key);
        if (frequency != null) {
            frequency.incrementAndGet();
        }
        
        return cache.get(key);
    }
    
    /**
     * Check memory usage and evict cache entries if necessary
     */
    private void checkAndManageMemoryUsage() {
        try {
            MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
            long used = heapMemoryUsage.getUsed();
            long max = heapMemoryUsage.getMax();
            
            // Calculate current memory usage percentage
            int usagePercentage = (int) ((used * 100) / max);
            
            // If memory usage exceeds threshold, evict entries
            if (usagePercentage > memoryThresholdPercentage) {
                int currentSize = cache.size();
                int entriesToRemove = currentSize / 4; // Remove 25% of entries
                
                logger.info("Memory usage at {}%, evicting {} entries from cache", 
                           usagePercentage, entriesToRemove);
                
                evictLeastFrequentlyUsed(entriesToRemove);
            }
        } catch (Exception e) {
            logger.error("Error during memory usage check", e);
        }
    }
    
    /**
     * Evict least frequently used entries from the cache
     * @param count number of entries to evict
     */
    private void evictLeastFrequentlyUsed(int count) {
        if (count <= 0 || cache.isEmpty()) {
            return;
        }
        
        // Find the entries with the lowest access frequencies
        accessFrequency.entrySet().stream()
            .sorted(Map.Entry.comparingByValue((a1, a2) -> Integer.compare(a1.get(), a2.get())))
            .limit(count)
            .map(Map.Entry::getKey)
            .forEach(key -> {
                cache.remove(key);
                accessFrequency.remove(key);
            });
    }
    
    /**
     * Get the current cache size
     * @return number of entries in the cache
     */
    public int size() {
        return cache.size();
    }
}