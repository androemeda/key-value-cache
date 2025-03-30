# Key-Value Cache Service

An in-memory Key-Value Cache service that implements basic operations like put(key, value) and get(key).

## Design Choices and Optimizations

1. **LRU Cache Implementation**: Used a LinkedHashMap with access-order to implement an LRU (Least Recently Used) eviction policy, ensuring that less recently used entries are removed first when the cache size limit is reached.

2. **Memory Management**: The service monitors heap memory usage and automatically evicts cache entries when memory usage exceeds a configurable threshold (default: 70%). This prevents the service from running out of memory.

3. **Thread Safety**: Used a ReadWriteLock to ensure thread safety while maintaining high throughput. Read operations don't block each other, only write operations do.

4. **Configurable Parameters**: Key cache parameters can be configured via application properties:
   - `cache.max.entries`: Maximum number of entries allowed in the cache
   - `cache.memory.threshold`: Memory usage threshold percentage for triggering eviction
   - `cache.memory.check.frequency`: How often to check memory usage (in number of operations)

5. **Input Validation**: The service validates that keys and values don't exceed the maximum length of 256 characters.

## Building the Docker Image

To build the Docker image, run:

```bash
mvn clean package
docker build -t key-value-cache .