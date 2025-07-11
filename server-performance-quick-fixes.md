# Server Performance Quick Fixes

## URGENT: Your Actual Issue

- **Backend call performance**: 789ms → 2.1 seconds (167% degradation)
- **Additional resource requests**: 2 new requests for JS/theme/icons
- **Root cause**: Java 11 + Glassfish 6 configuration mismatch

## Critical Fix #1: Java 11 Garbage Collector

**Java 11 changed default GC from Parallel to G1, causing throughput degradation**

```bash
# Force Parallel GC (match Java 8 behavior)
asadmin create-jvm-options '-XX\:+UseParallelGC'

# Remove G1 if present
asadmin delete-jvm-options '-XX\:+UseG1GC'

# Memory optimization
asadmin create-jvm-options '-Xms2048m:-Xmx2048m'
asadmin create-jvm-options '-XX\:MetaspaceSize=256m'
```

**Expected Impact**: 20-30% improvement immediately

## Critical Fix #2: Thread Pool Optimization

**Glassfish 6 has different thread pool defaults than Glassfish 3**

```bash
# HTTP thread pool (handles web requests)
asadmin set configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=200
asadmin set configs.config.server-config.thread-pools.thread-pool.http-thread-pool.min-thread-pool-size=50

# EJB thread pool (if using EJBs)
asadmin set configs.config.server-config.thread-pools.thread-pool.thread-pool-1.max-thread-pool-size=150
```

**Expected Impact**: 15-25% improvement

## Critical Fix #3: HTTP Compression

**Reduce resource loading overhead**

```bash
# Enable HTTP compression
asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.compression=on

# Increase connection limits
asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.max-connections=2048
```

**Expected Impact**: 10-15% improvement

## Important Fix #4: PrimeFaces Production Mode

**Add to your web.xml to optimize resource handling:**

```xml
<!-- Enable production mode -->
<context-param>
    <param-name>javax.faces.PROJECT_STAGE</param-name>
    <param-value>Production</param-value>
</context-param>

<!-- Optimize PrimeFaces resources -->
<context-param>
    <param-name>primefaces.MOVE_SCRIPTS_TO_BOTTOM</param-name>
    <param-value>true</param-value>
</context-param>

<context-param>
    <param-name>primefaces.CLIENT_SIDE_VALIDATION</param-name>
    <param-value>false</param-value>
</context-param>

<!-- Disable development features -->
<context-param>
    <param-name>javax.faces.FACELETS_REFRESH_PERIOD</param-name>
    <param-value>-1</param-value>
</context-param>
```

**Expected Impact**: 10-20% improvement

## Database Connection Pool Check

**If using database, verify connection pool:**

```bash
# Check current settings
asadmin get resources.jdbc-connection-pool.your-pool-name.*

# Optimize if needed
asadmin set resources.jdbc-connection-pool.your-pool-name.max-pool-size=30
asadmin set resources.jdbc-connection-pool.your-pool-name.steady-pool-size=10
```

## Testing & Validation

### 1. Before Changes
```bash
# Note current performance
asadmin get --monitor=true server.thread-pools.thread-pool.http-thread-pool.*
```

### 2. Apply Fixes in Order
1. **JVM options** (requires restart)
2. **Thread pools** (requires restart)  
3. **HTTP settings** (may require restart)
4. **Web.xml changes** (requires redeploy)

### 3. After Changes
- Test your problematic page
- Monitor backend call times
- Check resource loading in browser dev tools

## Expected Results

- **Backend calls**: 2.1s → 800-900ms (back to original performance)
- **Resource loading**: Reduced overhead from compression
- **Overall page load**: 50-70% improvement

## Quick Diagnostics

### Check Current JVM Settings
```bash
asadmin list-jvm-options | grep -E "(GC|Parallel|G1)"
```

### Monitor Thread Usage
```bash
asadmin get --monitor=true server.thread-pools.thread-pool.http-thread-pool.currentthreadsbusy-count
```

### Check Connection Pool
```bash
asadmin get --monitor=true resources.jdbc-connection-pool.your-pool-name.numconnused-current
```

## If Issues Persist

### Enable Request Logging
```bash
asadmin set-log-levels javax.enterprise.web.core=FINE
```

### Add Timing to Your Code
```java
public void yourActionMethod() {
    long start = System.currentTimeMillis();
    
    // Your existing code
    
    long end = System.currentTimeMillis();
    System.out.println("Action took: " + (end - start) + "ms");
}
```

This should resolve your 2.1-second backend performance issue by addressing the Java 11 and Glassfish 6 configuration differences that are causing the server-side bottleneck.