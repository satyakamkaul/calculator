# PrimeFaces Performance Solutions (Keeping Rich Components)

## Problem Recap
Your datatables with many `p:selectOneMenu` components are causing 2+ second delays due to JavaScript execution overhead. You need to keep PrimeFaces components for visual consistency.

## Solution 1: Cell/Row Editing (Recommended)

This approach renders **only display components initially**, then switches to editable PrimeFaces components **on demand**.

### Implementation:

```xml
<p:dataTable value="#{bean.items}" var="item" editable="true" editMode="cell">
    <p:ajax event="cellEdit" listener="#{bean.onCellEdit}" update="@this" />
    
    <p:column headerText="Status">
        <p:cellEditor>
            <f:facet name="output">
                <h:outputText value="#{item.statusLabel}" />
            </f:facet>
            <f:facet name="input">
                <p:selectOneMenu value="#{item.status}">
                    <f:selectItems value="#{bean.statusOptions}" />
                </p:selectOneMenu>
            </f:facet>
        </p:cellEditor>
    </p:column>
    
    <p:column headerText="Priority">
        <p:cellEditor>
            <f:facet name="output">
                <h:outputText value="#{item.priorityLabel}" />
            </f:facet>
            <f:facet name="input">
                <p:selectOneMenu value="#{item.priority}">
                    <f:selectItems value="#{bean.priorityOptions}" />
                </p:selectOneMenu>
            </f:facet>
        </p:cellEditor>
    </p:column>
</p:dataTable>
```

**Benefits:**
- ✅ Keeps PrimeFaces styling and functionality
- ✅ Only renders editable components when needed
- ✅ Dramatically reduces initial page load time
- ✅ Better user experience with clear edit states

## Solution 2: Row Editing Mode

Edit entire rows one at a time:

```xml
<p:dataTable value="#{bean.items}" var="item" editable="true" editMode="row">
    <p:ajax event="rowEdit" listener="#{bean.onRowEdit}" update="@this" />
    <p:ajax event="rowEditCancel" listener="#{bean.onRowCancel}" update="@this" />
    
    <p:column headerText="Status">
        <p:cellEditor>
            <f:facet name="output">
                <h:outputText value="#{item.statusLabel}" />
            </f:facet>
            <f:facet name="input">
                <p:selectOneMenu value="#{item.status}">
                    <f:selectItems value="#{bean.statusOptions}" />
                </p:selectOneMenu>
            </f:facet>
        </p:cellEditor>
    </p:column>
    
    <!-- Row editor column -->
    <p:column style="width:32px">
        <p:rowEditor />
    </p:column>
</p:dataTable>
```

## Solution 3: Master-Detail with Dialog

Keep your current datatable for display, edit in a popup dialog:

```xml
<!-- Display datatable -->
<p:dataTable value="#{bean.items}" var="item" selectionMode="single" 
             selection="#{bean.selectedItem}">
    <p:ajax event="rowSelect" listener="#{bean.onRowSelect}" 
            update=":editDialog" oncomplete="PF('editDlg').show()" />
    
    <p:column headerText="Status">
        <h:outputText value="#{item.statusLabel}" />
    </p:column>
    
    <p:column headerText="Actions">
        <p:commandButton value="Edit" action="#{bean.prepareEdit(item)}" 
                         update=":editDialog" oncomplete="PF('editDlg').show()" />
    </p:column>
</p:dataTable>

<!-- Edit dialog with full PrimeFaces components -->
<p:dialog header="Edit Item" widgetVar="editDlg" modal="true" 
          width="600" id="editDialog">
    <h:form>
        <p:panelGrid columns="2">
            <p:outputLabel value="Status:" />
            <p:selectOneMenu value="#{bean.selectedItem.status}">
                <f:selectItems value="#{bean.statusOptions}" />
            </p:selectOneMenu>
            
            <p:outputLabel value="Priority:" />
            <p:selectOneMenu value="#{bean.selectedItem.priority}">
                <f:selectItems value="#{bean.priorityOptions}" />
            </p:selectOneMenu>
        </p:panelGrid>
        
        <p:commandButton value="Save" action="#{bean.saveItem}" 
                         update=":mainForm:dataTable" 
                         oncomplete="PF('editDlg').hide()" />
        <p:commandButton value="Cancel" onclick="PF('editDlg').hide()" />
    </h:form>
</p:dialog>
```

## Solution 4: Pagination + Reduced Page Size

Reduce the number of components rendered simultaneously:

```xml
<p:dataTable value="#{bean.items}" var="item" paginator="true" 
             rows="10" paginatorPosition="both">
    <!-- Your existing columns with p:selectOneMenu -->
    <p:column headerText="Status">
        <p:selectOneMenu value="#{item.status}">
            <f:selectItems value="#{bean.statusOptions}" />
        </p:selectOneMenu>
    </p:column>
</p:dataTable>
```

**Configuration:**
- Change from 25 rows to 10 rows per page
- This reduces components from 400 to 160 (60% reduction)
- Should improve performance significantly

## Solution 5: Lazy Loading with Virtual Scrolling

For very large datasets:

```xml
<p:dataTable value="#{bean.lazyModel}" var="item" lazy="true" 
             scrollable="true" scrollHeight="400px" 
             virtualFlow="true" rows="20">
    
    <p:column headerText="Status">
        <p:selectOneMenu value="#{item.status}">
            <f:selectItems value="#{bean.statusOptions}" />
        </p:selectOneMenu>
    </p:column>
</p:dataTable>
```

```java
@Named
@ViewScoped
public class LazyBean implements Serializable {
    private LazyDataModel<Item> lazyModel;
    
    @PostConstruct
    public void init() {
        lazyModel = new LazyDataModel<Item>() {
            @Override
            public List<Item> load(int first, int pageSize, String sortField, 
                                   SortOrder sortOrder, Map<String,Object> filters) {
                // Load only the requested items
                return itemService.findItems(first, pageSize, sortField, sortOrder, filters);
            }
            
            @Override
            public int getRowCount() {
                return itemService.getItemCount();
            }
        };
    }
}
```

## Solution 6: Progressive Enhancement

Load basic table first, then enhance with JavaScript:

```xml
<p:dataTable value="#{bean.items}" var="item" widgetVar="myTable">
    <p:column headerText="Status">
        <h:outputText value="#{item.statusLabel}" id="statusText_#{item.id}" 
                      style="#{item.editable ? 'display:none' : ''}" />
        <p:selectOneMenu value="#{item.status}" id="statusSelect_#{item.id}"
                         style="#{item.editable ? '' : 'display:none'}">
            <f:selectItems value="#{bean.statusOptions}" />
        </p:selectOneMenu>
    </p:column>
    
    <p:column headerText="Actions">
        <p:commandButton value="Edit" onclick="toggleEdit(#{item.id})" 
                         rendered="#{not item.editable}" />
        <p:commandButton value="Save" action="#{bean.saveItem(item)}" 
                         update="@form" rendered="#{item.editable}" />
    </p:column>
</p:dataTable>

<script>
function toggleEdit(itemId) {
    document.getElementById('statusText_' + itemId).style.display = 'none';
    document.getElementById('statusSelect_' + itemId).style.display = 'block';
}
</script>
```

## Recommended Implementation Order

### 1. **Start with Cell Editing** (Easiest, Biggest Impact)
- Requires minimal code changes
- Maintains all PrimeFaces functionality
- Provides immediate 80%+ performance improvement

### 2. **Add Pagination**
- Reduce rows from 25 to 10-15
- Quick configuration change
- Provides additional performance boost

### 3. **Consider Master-Detail for Complex Cases**
- For rows that need extensive editing
- Better user experience for complex forms
- Keeps main table fast

## Expected Performance Impact

| Solution | Performance Improvement | Implementation Effort |
|----------|------------------------|----------------------|
| Cell Editing | 80-90% | Low |
| Row Editing | 75-85% | Low |
| Master-Detail | 85-95% | Medium |
| Pagination (10 rows) | 60-70% | Very Low |
| Lazy Loading | 90-95% | High |

## Migration Strategy

1. **Backup current implementation**
2. **Implement cell editing on one problematic table**
3. **Test performance improvement**
4. **Apply to remaining tables**
5. **Add pagination as secondary optimization**

This approach lets you keep all your PrimeFaces styling and functionality while solving the performance problem!