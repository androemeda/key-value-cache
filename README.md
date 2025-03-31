# Key-Value Cache Service

An in-memory Key-Value Cache service that implements basic operations like put(key, value) and get(key).

## Building the Docker Image

clone repo:
```bash
git clone git@github.com:androemeda/key-value-cache.git
cd key-value-cache
```

Build with Maven:
```bash
mvn clean package
```

To build the Docker image, run:

```bash
docker build -t key-value-cache:1.0.0 .
```

Running the Application
```bash
docker run -p 7171:7171 key-value-cache:1.0.0
```

Using Docker Hub Image
Pull the pre-built image:
```bash
docker pull kartik271/key-value-cache:1.0.0
docker run -p 7171:7171 kartik271/key-value-cache:1.0.0
```

For AMD64 systems specifically:
```bash
docker pull kartik271/key-value-cache:1.0.0-amd64
docker run -p 7171:7171 kartik271/key-value-cache:1.0.0-amd64
```

### Running the Docker Container
To run the Docker container:

```bash
docker run -p 7171:7171 key-value-cache
```

For AMD64 systems specifically:

```bash
docker pull kartik271/key-value-cache:1.0.0-amd64
docker run -p 7171:7171 kartik271/key-value-cache:1.0.0-amd64
```


## Design Choices and Optimizations

1. **Cache Segmentation**: Split cache into 16 independent segments based on key hash to reduce contention and improve throughput.

2. **Concurrency Optimizations**:  Used ConcurrentHashMap with optimized parameters and eliminated explicit locks for high-concurrency performance.

3. **Memory Efficiency**: Pre-allocated data structures, enforced string length limits, and configured JVM for optimal memory usage on 2GB instances.

4. **Response Time Improvements**: Pre-created common response objects, implemented fast-path validation, and cached responses to avoid object creation.

5. **GC Tuning**: Configured G1GC with low pause times, set heap size boundaries, and enabled string deduplication to reduce latency spikes.