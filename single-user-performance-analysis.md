# Single User Performance Analysis

## The Real Problem

You're experiencing **2+ second delays even with a single user** and no application load. This means the issue is **not concurrency-related** but rather:

- **Database query performance degradation**
- **JVM/Application initialization overhead**  
- **Resource loading/caching issues**
- **Code-level processing changes**

## Most Likely Culprit: Database Performance

### Database Connection & Query Issues

Since your backend call went from 789ms → 2.1s, the database is the prime suspect:

#### 1. Database Connection Pool Initialization
```bash
# Check current connection pool settings
asadmin get resources.jdbc-connection-pool.your-pool-name.*

# Look for these potential issues:
# - connection-validation-method (may be causing overhead)
# - validate-atmost-once-period (validation frequency)
# - connection-creation-retry-attempts (retry overhead)
```

#### 2. Connection Validation Overhead
```bash
# Disable expensive connection validation
asadmin set resources.jdbc-connection-pool.your-pool-name.is-connection-validation-required=false

# Or use lighter validation
asadmin set resources.jdbc-connection-pool.your-pool-name.connection-validation-method=meta-data
```

#### 3. Database Driver Compatibility
Java 11 + new database drivers might have compatibility issues:

```xml
<!-- Check your database driver version in pom.xml -->
<!-- Ensure you're using Java 11 compatible driver -->
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc11</artifactId>
    <!-- Use latest version for Java 11 -->
</dependency>
```

### Database Query Performance Analysis

#### Enable Query Logging
Add to your `persistence.xml`:

```xml
<properties>
    <!-- Log all SQL queries with timing -->
    <property name="eclipselink.logging.level.sql" value="FINE"/>
    <property name="eclipselink.logging.parameters" value="true"/>
    <property name="eclipselink.logging.timestamp" value="true"/>
    
    <!-- Or for Hibernate -->
    <property name="hibernate.show_sql" value="true"/>
    <property name="hibernate.format_sql" value="true"/>
    <property name="hibernate.use_sql_comments" value="true"/>
</properties>
```

#### Check for Query Plan Changes
Your database might have changed execution plans:

```sql
-- For Oracle
EXPLAIN PLAN FOR your_slow_query;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

-- Look for:
-- - Table scans instead of index usage
-- - Different join orders
-- - Missing statistics
```

## JVM Initialization Issues

### 1. Class Loading Overhead
Java 11 has different class loading behavior:

```bash
# Enable class loading logging
asadmin create-jvm-options '-Xlog:class+load:gc.log:time'

# Reduce verification overhead
asadmin create-jvm-options '-Xverify:none'
```

### 2. JIT Compilation Differences
```bash
# Force server compilation mode
asadmin create-jvm-options '-server'

# Reduce compilation threshold for faster warmup
asadmin create-jvm-options '-XX:CompileThreshold=1000'
```

### 3. Memory Allocation Issues
```bash
# Pre-allocate memory to avoid runtime allocation
asadmin create-jvm-options '-Xms2048m:-Xmx2048m'

# Use large pages for better memory performance
asadmin create-jvm-options '-XX:+UseLargePages'
```

## Application-Level Issues

### 1. CDI Bean Initialization
Check if your CDI beans are taking longer to initialize:

```java
@Named
@ApplicationScoped // Consider scope changes
public class YourBean {
    
    @PostConstruct
    public void init() {
        long start = System.currentTimeMillis();
        
        // Your initialization code
        
        long end = System.currentTimeMillis();
        System.out.println("Bean init took: " + (end - start) + "ms");
    }
}
```

### 2. Lazy Loading Issues
If using JPA lazy loading, check for N+1 query problems:

```java
// Add fetch joins to avoid lazy loading overhead
@Query("SELECT e FROM Entity e JOIN FETCH e.relatedEntities WHERE e.id = :id")
Entity findEntityWithRelations(@Param("id") Long id);
```

### 3. Transaction Overhead
Check transaction configuration:

```xml
<!-- In persistence.xml - ensure proper transaction handling -->
<property name="eclipselink.transaction.join-existing" value="true"/>
<property name="eclipselink.connection-pool.initial" value="5"/>
<property name="eclipselink.connection-pool.min" value="5"/>
```

## PrimeFaces Resource Loading Issues

### 1. Resource Caching Problems
PrimeFaces 12 might be reloading resources:

```xml
<!-- In web.xml -->
<context-param>
    <param-name>javax.faces.RESOURCE_CACHE_SIZE</param-name>
    <param-value>2048</param-value>
</context-param>

<context-param>
    <param-name>primefaces.CACHE_PROVIDER</param-name>
    <param-value>org.primefaces.cache.DefaultCacheProvider</param-value>
</context-param>
```

### 2. Theme Loading Overhead
```xml
<context-param>
    <param-name>primefaces.THEME</param-name>
    <param-value>your-theme</param-value>
</context-param>

<!-- Disable theme switching -->
<context-param>
    <param-name>primefaces.THEME_PER_FORM</param-name>
    <param-value>false</param-value>
</context-param>
```

## Diagnostic Steps

### 1. Add Detailed Timing
Add timing to your action methods:

```java
public String yourActionMethod() {
    long totalStart = System.currentTimeMillis();
    
    // Database operation
    long dbStart = System.currentTimeMillis();
    // Your database code here
    long dbEnd = System.currentTimeMillis();
    System.out.println("Database time: " + (dbEnd - dbStart) + "ms");
    
    // Business logic
    long logicStart = System.currentTimeMillis();
    // Your business logic here
    long logicEnd = System.currentTimeMillis();
    System.out.println("Logic time: " + (logicEnd - logicStart) + "ms");
    
    long totalEnd = System.currentTimeMillis();
    System.out.println("Total time: " + (totalEnd - totalStart) + "ms");
    
    return "success";
}
```

### 2. Database Connection Testing
```java
@Named
public class ConnectionTestBean {
    
    @PersistenceContext
    private EntityManager em;
    
    public void testConnection() {
        long start = System.currentTimeMillis();
        
        // Simple query to test connection speed
        em.createNativeQuery("SELECT 1 FROM DUAL").getSingleResult();
        
        long end = System.currentTimeMillis();
        System.out.println("Connection test took: " + (end - start) + "ms");
    }
}
```

### 3. Enable Detailed Server Logging
```bash
# Enable request processing logging
asadmin set-log-levels javax.enterprise.web.core=FINEST
asadmin set-log-levels javax.enterprise.resource.corba=FINE

# Monitor the server.log for timing information
tail -f domain/logs/server.log | grep -E "(time|duration|ms)"
```

## Quick Elimination Tests

### Test 1: Database Only
Create a simple servlet that just does a database query:

```java
@WebServlet("/dbtest")
public class DatabaseTestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        long start = System.currentTimeMillis();
        
        // Your database query here
        
        long end = System.currentTimeMillis();
        response.getWriter().println("DB Query time: " + (end - start) + "ms");
    }
}
```

### Test 2: Static Content Only  
Create a simple JSF page with no database calls:

```xml
<h:outputText value="#{facesContext.externalContext.requestContextPath}" />
```

### Test 3: Minimal PrimeFaces
Test with basic PrimeFaces components:

```xml
<p:outputPanel>
    <h:outputText value="Test: #{bean.testValue}" />
</p:outputPanel>
```

## Expected Findings

Based on the symptoms, you'll likely find:

1. **Database connection initialization** taking 1+ seconds
2. **Specific queries** running much slower
3. **Resource loading** happening on every request
4. **Bean initialization** taking excessive time

## Most Likely Fix

The issue is probably **database connection overhead** or **specific query performance degradation**. Focus on:

1. **Connection pool configuration**
2. **Database driver compatibility**
3. **Query execution plan analysis**
4. **JPA/ORM configuration changes**

This will pinpoint exactly where those 2 seconds are being spent!