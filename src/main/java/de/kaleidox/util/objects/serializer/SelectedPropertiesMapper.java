// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects.serializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SelectedPropertiesMapper extends PropertiesMapper {
    private String selected;

    public SelectedPropertiesMapper(final IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort, final Object selected) {
        super(ioPort);
        this.selected = selected.toString();
    }

    public SelectedPropertiesMapper(final File file, final Object selected) {
        super(file);
        this.selected = selected.toString();
    }

    public SelectedPropertiesMapper(final File file, final Character splitWith, final Object selected) {
        super(file, splitWith);
        this.selected = selected.toString();
    }

    public SelectedPropertiesMapper(final IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort, final Character splitWith, final Object selected) {
        super(ioPort, splitWith);
        this.selected = selected.toString();
    }

    public PropertiesMapper add(final Object item) {
        if (this.selected.isEmpty()) {
            throw new NullPointerException("No Key selected.");
        }
        this.add(this.selected, item);
        return this;
    }

    public String get(final int index) {
        if (this.selected.isEmpty()) {
            throw new NullPointerException("No Key selected.");
        }
        return this.get(this.selected, index);
    }

    public String softGet(final int index, final Object valueIfAbsent) {
        if (this.selected.isEmpty()) {
            throw new NullPointerException("No Key selected.");
        }
        return this.softGet(this.selected, index, valueIfAbsent);
    }

    public String set(final int index, final Object value) {
        if (this.selected.isEmpty()) {
            throw new NullPointerException("No Key selected.");
        }
        return this.set(this.selected, index, value);
    }

    public void addAll(final ArrayList<Object> values) {
        this.add(this.selected, values);
    }

    public ArrayList<String> set(final ArrayList<String> newValues) {
        return super.set(this.selected, newValues);
    }

    public List<String> getAll() {
        if (this.selected.isEmpty()) {
            throw new NullPointerException("No Key selected.");
        }
        return this.getAll(this.selected);
    }

    public PropertiesMapper clear() {
        if (this.selected.isEmpty()) {
            throw new NullPointerException("No Key selected.");
        }
        return this.clear(this.selected);
    }

    public int size() {
        if (this.selected.isEmpty()) {
            throw new NullPointerException("No Key selected.");
        }
        return this.size(this.selected);
    }

    public void remove(final int index) {
        if (this.selected.isEmpty()) {
            throw new NullPointerException("No Key selected.");
        }
        this.remove(this.selected, index);
    }

    @Override
    public boolean containsValue(final Object value) {
        if (this.selected == null) {
            throw new NullPointerException("No Key selected.");
        }
        try {
            return this.map.get(this.selected).contains(value.toString());
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public void write() {
        super.write();
    }

    public PropertiesMapper removeValue(final Object value) {
        if (this.selected == null) {
            throw new NullPointerException("No Key selected.");
        }
        return this.removeValue(this.selected, value);
    }
}
