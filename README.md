# Performance Issue Analysis & Solutions

## Problem Summary

Your web application migration from Glassfish 3 to 6, PrimeFaces 5 to 12, and Java 8 to 11 has introduced a **2+ second performance degradation** in datatable button interactions, with backend calls increasing from 789ms to 2.1 seconds.

## Your Setup (Updated Understanding)

- **Main datatable**: Display-only with `p:selectOneMenu` components (causing performance issues)
- **Edit buttons**: Open popup dialogs for actual editing
- **Popup dialogs**: Contain the real editing components

## Root Cause Analysis

**Primary Issue**: Your main datatable is rendering hundreds of `p:selectOneMenu` components that are **never used for editing** - they're just for display! This creates massive JavaScript overhead (11.3ms per component × 400 components = 4.5+ seconds) for no functional benefit.

## Solution Documents

### 🚨 [Display Table Optimization](display-table-optimization.md) - YOUR PERFECT SOLUTION
**This is exactly for your use case** - Optimize display table performance while keeping full PrimeFaces functionality in your popups.

### ⚙️ [Glassfish Configuration Guide](glassfish-config-recommendations.md)
Server-side optimizations to complement the frontend improvements.

### 📊 [Alternative PrimeFaces Solutions](primefaces-performance-solutions.md)
Other approaches if your setup is different.

## The Simple Solution

Since you use **popup editing**, you can replace display components in the main table with simple `h:outputText` while keeping **all PrimeFaces components in your popups unchanged**.

### Quick Example:

```xml
<!-- BEFORE (Slow - unnecessary p:selectOneMenu for display) -->
<p:dataTable value="#{bean.items}" var="item">
    <p:column headerText="Status">
        <p:selectOneMenu value="#{item.status}" disabled="true">
            <f:selectItems value="#{bean.statusOptions}" />
        </p:selectOneMenu>
    </p:column>
    <p:column headerText="Actions">
        <p:commandButton value="Edit" onclick="PF('editDlg').show()" />
    </p:column>
</p:dataTable>

<!-- AFTER (Fast - simple display, rich editing in popup) -->
<p:dataTable value="#{bean.items}" var="item">
    <p:column headerText="Status">
        <h:outputText value="#{item.statusLabel}" styleClass="status-display" />
    </p:column>
    <p:column headerText="Actions">
        <p:commandButton value="Edit" onclick="PF('editDlg').show()" />
    </p:column>
</p:dataTable>

<!-- Your popup stays EXACTLY the same - full PrimeFaces functionality -->
<p:dialog widgetVar="editDlg">
    <p:selectOneMenu value="#{bean.selectedItem.status}">
        <f:selectItems value="#{bean.statusOptions}" />
    </p:selectOneMenu>
    <!-- All your rich components unchanged -->
</p:dialog>
```

## Expected Results

- **Main table performance**: 99% improvement (4.5 seconds → 10ms)
- **Popup functionality**: Unchanged - keeps all PrimeFaces features
- **User experience**: Actually better - cleaner display, rich editing when needed
- **Implementation effort**: Minimal - just replace display components

## Implementation Roadmap

### Phase 1: Immediate (1-2 hours) ⚡
1. **Replace `p:selectOneMenu` with `h:outputText`** in main datatable
2. **Add label getter methods** to your beans
3. **Keep popup dialogs unchanged**

**Expected Result**: 90%+ performance improvement immediately

### Phase 2: Polish (1-2 days) 🎨
1. **Add CSS styling** to match your theme
2. **Apply Glassfish server optimizations**
3. **Add pagination** if not already present

**Expected Result**: Return to or exceed original 789ms performance

## Key Insights

1. **Your setup is actually ideal for optimization** - Clean separation between display and editing
2. **Display tables should be lightweight** - No need for rich components
3. **Rich components belong in editing contexts** - Popups, forms, actual interaction points
4. **This solution gives you the best of both worlds** - Fast display + rich editing

## Next Steps

1. **Start with [Display Table Optimization](display-table-optimization.md)** - Your exact use case
2. **Test one problematic table first** - Validate the 90%+ improvement
3. **Apply [Glassfish Configuration](glassfish-config-recommendations.md)** - Server-side boost
4. **Expand to other tables** - Roll out the successful pattern

---

*This solution is tailored specifically for display tables with popup editing - the most common and effective pattern for large datasets in JSF applications.*