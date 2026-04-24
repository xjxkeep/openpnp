package org.openpnp.gui.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import org.openpnp.model.Configuration;
import org.openpnp.model.Part;

@SuppressWarnings("serial")
public class SearchablePartsComboBoxModel extends AbstractListModel<Part>
        implements ComboBoxModel<Part>, PropertyChangeListener {
    private final IdentifiableComparator<Part> comparator = new IdentifiableComparator<>();
    private List<Part> allParts = new ArrayList<>();
    private List<Part> filteredParts = new ArrayList<>();
    private Object selectedItem;
    private String filterText = "";

    public SearchablePartsComboBoxModel() {
        load();
        Configuration.get().addPropertyChangeListener("parts", this);
    }

    private void load() {
        allParts = new ArrayList<>(Configuration.get().getParts());
        Collections.sort(allParts, comparator);
        applyFilter();
    }

    public void setFilter(String text) {
        String newFilter = text == null ? "" : text.toLowerCase(Locale.ROOT);
        if (newFilter.equals(filterText)) {
            return;
        }
        filterText = newFilter;
        applyFilter();
    }

    private void applyFilter() {
        int oldSize = filteredParts.size();
        List<Part> result;
        if (filterText.isEmpty()) {
            result = new ArrayList<>(allParts);
        } else {
            result = new ArrayList<>();
            for (Part p : allParts) {
                String id = p.getId();
                if (id != null && id.toLowerCase(Locale.ROOT).contains(filterText)) {
                    result.add(p);
                }
            }
        }
        filteredParts = result;
        if (oldSize > 0) {
            fireIntervalRemoved(this, 0, oldSize - 1);
        }
        if (!filteredParts.isEmpty()) {
            fireIntervalAdded(this, 0, filteredParts.size() - 1);
        }
    }

    @Override
    public int getSize() {
        return filteredParts.size();
    }

    @Override
    public Part getElementAt(int index) {
        return filteredParts.get(index);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if ((selectedItem != null && !selectedItem.equals(anItem))
                || (selectedItem == null && anItem != null)) {
            selectedItem = anItem;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        load();
    }
}
