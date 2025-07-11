# Glassfish Configuration Recommendations

## Domain.xml Configuration Changes

Based on your migration from Glassfish 3 to 6, here are the key configuration changes to implement in your domain.xml file or via asadmin commands.

## JVM Options Section

Add these JVM options to your domain.xml under `<java-config>`:

```xml
<java-config>
    <!-- Existing options... -->
    
    <!-- Memory Management -->
    <jvm-options>-Xms2048m</jvm-options>
    <jvm-options>-Xmx2048m</jvm-options>
    <jvm-options>-XX:MetaspaceSize=256m</jvm-options>
    <jvm-options>-XX:MaxMetaspaceSize=2g</jvm-options>
    
    <!-- Garbage Collection -->
    <jvm-options>-XX:+UseG1GC</jvm-options>
    <jvm-options>-XX:+UseStringDeduplication</jvm-options>
    <jvm-options>-XX:+DisableExplicitGC</jvm-options>
    
    <!-- Performance Optimizations -->
    <jvm-options>-Dfish.payara.classloading.delegate=false</jvm-options>
    <jvm-options>-Djdk.tls.rejectClientInitiatedRenegotiation=true</jvm-options>
    <jvm-options>-Xss512k</jvm-options>
    
    <!-- Remove if present -->
    <!-- <jvm-options>-Xmx512m</jvm-options> -->
</java-config>
```

## HTTP Service Configuration

Update your HTTP listeners and thread pools:

```xml
<thread-pools>
    <thread-pool max-thread-pool-size="350" 
                 min-thread-pool-size="25" 
                 thread-pool-id="http-thread-pool">
    </thread-pool>
    
    <thread-pool max-thread-pool-size="250" 
                 min-thread-pool-size="150" 
                 thread-pool-id="thread-pool-1">
    </thread-pool>
</thread-pools>

<protocols>
    <protocol name="http-listener-1">
        <http max-connections="500" 
              timeout-seconds="60"
              file-cache-enabled="true">
            <file-cache max-age-seconds="3600" />
        </http>
    </protocol>
</protocols>
```

## EJB Container Configuration

```xml
<ejb-container max-pool-size="120" 
               steady-pool-size="10" 
               pool-resize-quantity="2">
</ejb-container>
```

## Development Features (Disable for Production)

```xml
<admin-service>
    <das-config autodeploy-enabled="false" 
                dynamic-reload-enabled="false">
    </das-config>
</admin-service>
```

## Asadmin Commands Alternative

If you prefer using asadmin commands instead of editing domain.xml directly:

```bash
# JVM Options
asadmin delete-jvm-options '-Xmx512m'
asadmin create-jvm-options '-Xms2048m:-Xmx2048m'
asadmin create-jvm-options '-XX\:MetaspaceSize=256m:-XX\:MaxMetaspaceSize=2g'
asadmin create-jvm-options '-XX\:+UseG1GC:-XX\:+UseStringDeduplication'
asadmin create-jvm-options '-XX\:+DisableExplicitGC'
asadmin create-jvm-options '-Dfish.payara.classloading.delegate=false'

# Thread Pools
asadmin set configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=350
asadmin set configs.config.server-config.thread-pools.thread-pool.http-thread-pool.min-thread-pool-size=25
asadmin set configs.config.server-config.thread-pools.thread-pool.thread-pool-1.max-thread-pool-size=250
asadmin set configs.config.server-config.thread-pools.thread-pool.thread-pool-1.min-thread-pool-size=150

# HTTP Configuration
asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.max-connections=500
asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.timeout-seconds=60
asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.file-cache.enabled=true
asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.file-cache.max-age-seconds=3600

# EJB Container
asadmin set configs.config.server-config.ejb-container.max-pool-size=120
asadmin set configs.config.server-config.ejb-container.steady-pool-size=10
asadmin set configs.config.server-config.ejb-container.pool-resize-quantity=2

# Disable Development Features
asadmin set configs.config.server-config.admin-service.das-config.dynamic-reload-enabled=false
asadmin set configs.config.server-config.admin-service.das-config.autodeploy-enabled=false
```

## Web.xml Changes

Update your default-web.xml file (in `domain/config/`) to disable JSP development mode:

```xml
<servlet>
    <servlet-name>jsp</servlet-name>
    <servlet-class>org.glassfish.wasp.servlet.JspServlet</servlet-class>
    <init-param>
        <param-name>development</param-name>
        <param-value>false</param-value>
    </init-param>
    <init-param>
        <param-name>genStrAsCharArray</param-name>
        <param-value>true</param-value>
    </init-param>
</servlet>
```

## Monitoring Configuration

Enable monitoring to track improvements:

```bash
asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.web-container=HIGH
asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.ejb-container=HIGH
asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.thread-pool=HIGH
asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.http-service=HIGH
```

## Verification Commands

After applying changes, verify your configuration:

```bash
# Check JVM options
asadmin list-jvm-options

# Check thread pool settings
asadmin get configs.config.server-config.thread-pools.thread-pool.http-thread-pool.*

# Check HTTP listener settings
asadmin get configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.*

# Check EJB container settings
asadmin get configs.config.server-config.ejb-container.*
```

## Implementation Order

1. **Apply JVM options first** (requires restart)
2. **Configure thread pools** (requires restart)
3. **Update HTTP settings** (may require restart)
4. **Test performance** after each step
5. **Apply EJB and other optimizations**

## Expected Impact

- **JVM tuning**: 10-15% improvement
- **Thread pool optimization**: 5-10% improvement  
- **HTTP connector tuning**: 5-15% improvement
- **Combined with PrimeFaces fixes**: Should resolve the 2+ second delay issue

These configurations are based on production-ready settings and should significantly improve your application's performance.