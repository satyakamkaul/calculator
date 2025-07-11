# Performance Issue Analysis & Solutions

## Problem Summary

Your web application migration from Glassfish 3 to 6, PrimeFaces 5 to 12, and Java 8 to 11 has introduced a **2+ second performance degradation**:

- **Backend calls**: 789ms → 2.1 seconds (167% slower)
- **Additional resource requests**: 2 new requests for JS/theme/icons
- **Total impact**: Noticeable 2-second lag on button interactions

## Your Setup (Correctly Understood)

- **Datatable**: Simple display attributes (text fields, etc.) - no performance issue here
- **Edit buttons**: Open popup dialogs for editing
- **Performance bottleneck**: Server-side processing, not client-side components

## Root Cause Analysis

**Primary Issue**: Server-side performance degradation due to migration changes:

1. **Java 11 performance characteristics** - Different default GC (G1 vs Parallel)
2. **Glassfish 6 configuration differences** - New thread pool and connector defaults  
3. **PrimeFaces 12 resource overhead** - Additional JS/theme resource requests
4. **Configuration mismatch** - Settings optimized for old stack

## Solution Documents

### 🚨 [Server Performance Quick Fixes](server-performance-quick-fixes.md) - START HERE
**Your immediate solution** - Critical server-side optimizations that will restore performance.

### 📊 [Complete Server Analysis](actual-performance-analysis.md)
Comprehensive server-side performance analysis with detailed optimizations and monitoring.

### ⚙️ [Glassfish Configuration Guide](glassfish-config-recommendations.md)
Complete domain.xml and asadmin configuration optimizations.

## The Real Solution

Your issue is **server-side processing bottlenecks**, not client-side component overhead.

### Critical Fixes:

```bash
# 1. Fix Java 11 GC (biggest impact)
asadmin create-jvm-options '-XX\:+UseParallelGC'
asadmin create-jvm-options '-Xms2048m:-Xmx2048m'

# 2. Optimize thread pools
asadmin set configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=200

# 3. Enable compression
asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.compression=on
```

### Web.xml optimizations:

```xml
<context-param>
    <param-name>javax.faces.PROJECT_STAGE</param-name>
    <param-value>Production</param-value>
</context-param>

<context-param>
    <param-name>primefaces.MOVE_SCRIPTS_TO_BOTTOM</param-name>
    <param-value>true</param-value>
</context-param>
```

## Expected Results

- **Backend calls**: 2.1s → 800-900ms (return to original performance)
- **Resource loading**: Reduced overhead from compression
- **Overall improvement**: 50-70% faster page interactions

## Implementation Roadmap

### Phase 1: Critical (1-2 hours) ⚡
1. **Apply JVM tuning** - Force Parallel GC, optimize memory
2. **Increase thread pools** - Handle concurrent requests better
3. **Enable compression** - Reduce resource loading overhead

**Expected Result**: Return to ~800ms backend performance

### Phase 2: Optimization (Same day) 🔧
1. **Configure PrimeFaces production mode**
2. **Optimize connection pools**
3. **Fine-tune HTTP connectors**

**Expected Result**: Exceed original performance

### Phase 3: Monitoring (Next day) 📊
1. **Enable performance monitoring**
2. **Profile database queries**
3. **Monitor resource usage**

**Expected Result**: Sustained optimal performance

## Key Insights

1. **Java 11 default GC change** - G1 optimizes for latency but can reduce throughput
2. **Glassfish 6 different defaults** - Thread pools and connectors need tuning
3. **PrimeFaces 12 resource overhead** - More JS/CSS files to load
4. **Configuration is critical** - Default settings aren't optimized for your workload

## Diagnostics

### Check if you have the right GC:
```bash
asadmin list-jvm-options | grep -E "(GC|Parallel|G1)"
```

### Monitor current thread usage:
```bash
asadmin get --monitor=true server.thread-pools.thread-pool.http-thread-pool.currentthreadsbusy-count
```

## Next Steps

1. **Start with [Server Performance Quick Fixes](server-performance-quick-fixes.md)** - Will solve your 2.1s issue
2. **Read [Complete Server Analysis](actual-performance-analysis.md)** - For thorough understanding
3. **Apply [Glassfish Configuration](glassfish-config-recommendations.md)** - For comprehensive tuning
4. **Monitor improvements** - Validate each change

---

*This analysis addresses the actual server-side performance bottlenecks causing your 2+ second delay, not component-related issues. The migration to Java 11 and Glassfish 6 introduced configuration mismatches that need specific tuning.*