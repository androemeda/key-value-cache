package com.cache.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.util.Iterator;

@Service
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    
    // Maximum length for keys and values as per requirements
    private static final int MAX_STRING_LENGTH = 256;
    
    // Default maximum cache entries (can be configured in application.properties)
    @Value("${cache.max.entries:100000}")
    private int maxEntries;
    
    // Memory threshold percentage - evict items when memory usage exceeds this percentage
    @Value("${cache.memory.threshold:70}")
    private int memoryThresholdPercentage;
    
    // Memory check frequency - check memory usage after these many operations
    @Value("${cache.memory.check.frequency:1000}")
    private int memoryCheckFrequency;
    
    // Operation counter used to trigger memory checks
    private int operationCounter = 0;
    
    // Memory bean for monitoring memory usage
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    
    // Lock for thread safety
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // LRU Cache implementation using LinkedHashMap
    private Map<String, String> cache = new LinkedHashMap<String, String>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > maxEntries;
        }
    };

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
        
        try {
            lock.writeLock().lock();
            cache.put(key, value);
            
            // Check memory usage periodically
            if (++operationCounter % memoryCheckFrequency == 0) {
                checkAndManageMemoryUsage();
            }
            
            return true;
        } finally {
            lock.writeLock().unlock();
        }
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
        
        try {
            lock.readLock().lock();
            return cache.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Check memory usage and evict cache entries if necessary
     */
    private void checkAndManageMemoryUsage() {
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
        long used = heapMemoryUsage.getUsed();
        long max = heapMemoryUsage.getMax();
        
        // Calculate current memory usage percentage
        int usagePercentage = (int) ((used * 100) / max);
        
        // If memory usage exceeds threshold, evict a percentage of entries
        if (usagePercentage > memoryThresholdPercentage) {
            int currentSize = cache.size();
            int entriesToRemove = currentSize / 4; // Remove 25% of entries
            
            logger.info("Memory usage at {}%, evicting {} entries from cache", 
                       usagePercentage, entriesToRemove);
            
            evictEntries(entriesToRemove);
        }
    }
    
    /**
     * Evict a specific number of entries from the cache (oldest entries first)
     * @param count number of entries to evict
     */
    private void evictEntries(int count) {
        int evicted = 0;
        try {
            lock.writeLock().lock();
            
            for (Iterator<Map.Entry<String, String>> it = cache.entrySet().iterator(); 
                 it.hasNext() && evicted < count; ) {
                it.next();
                it.remove();
                evicted++;
            }
        } finally {
            lock.writeLock().unlock();
        }
        
        logger.info("Evicted {} entries from cache", evicted);
    }
    
    /**
     * Get the current cache size
     * @return number of entries in the cache
     */
    public int size() {
        try {
            lock.readLock().lock();
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}