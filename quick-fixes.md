# Quick Fixes for 2-Second Performance Issue

## URGENT: Primary Fix (Will resolve 80% of the problem)

### Replace PrimeFaces components in datatables

**Problem**: Each `p:selectOneMenu` takes ~11ms of JavaScript execution time. With multiple components, this creates a 2+ second delay.

**Solution**: Replace with native HTML components in large datatables:

```xml
<!-- BEFORE (SLOW) -->
<p:dataTable value="#{bean.items}" var="item">
    <p:column>
        <p:selectOneMenu value="#{item.status}">
            <f:selectItems value="#{bean.statusOptions}" />
        </p:selectOneMenu>
    </p:column>
</p:dataTable>

<!-- AFTER (FAST) -->
<p:dataTable value="#{bean.items}" var="item">
    <p:column>
        <h:selectOneMenu value="#{item.status}" styleClass="form-control">
            <f:selectItems value="#{bean.statusOptions}" />
        </h:selectOneMenu>
    </p:column>
</p:dataTable>
```

## IMPORTANT: Server Configuration

### 1. JVM Options (Add to domain.xml or startup script)
```bash
-Xms2048m
-Xmx2048m
-XX:+UseG1GC
-XX:+UseStringDeduplication
-XX:+DisableExplicitGC
```

### 2. Use Production Domain (Glassfish Enterprise only)
```bash
./asadmin stop-domain domain1
./asadmin start-domain production
```

### 3. HTTP Thread Pool Optimization
```bash
asadmin set configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=350
```

## TESTING: Validate the Fix

1. **Before changes**: Note current page load time in browser dev tools
2. **Apply component changes**: Replace p:selectOneMenu in your slowest datatable
3. **After changes**: Measure improvement (should see 75%+ reduction)
4. **Apply server config**: Additional 10-20% improvement expected

## Expected Results

- **Immediate**: 2+ seconds → under 500ms for page interactions
- **Full optimization**: Should return to or exceed original 789ms performance

## If You Need Rich Components

Consider these alternatives:
- **Cell editing**: `<p:dataTable editable="true">` (uses native components during edit)
- **Row editing**: Edit one row at a time
- **Master-detail**: Edit in separate panel/dialog
- **Pagination**: Reduce rows per page to 10-15

The key insight: **Rich components are performance killers in large quantities**. Use them sparingly or in small datasets only.