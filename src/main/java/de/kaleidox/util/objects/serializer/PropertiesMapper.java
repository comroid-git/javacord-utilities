// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects.serializer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import de.kaleidox.util.Utils;
import de.kaleidox.util.libs.CustomCollectors;
import de.kaleidox.util.tools.Debugger;

public class PropertiesMapper {
    protected ConcurrentHashMap<String, String> map;
    protected ConcurrentHashMap<String, List<String>> values;
    protected IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort;
    private Character splitWith;
    private Debugger log;

    public PropertiesMapper(final IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort) {
        this(ioPort, '\u25aa');
    }

    public PropertiesMapper(final File file) {
        this(IOPort.mapPort(file), '\u25aa');
    }

    public PropertiesMapper(final String filePath) {
        this(IOPort.mapPort(new File(filePath)), '\u25aa');
    }

    public PropertiesMapper(final File file, final Character splitWith) {
        this(IOPort.mapPort(file), '\u25aa');
    }

    public PropertiesMapper(final IOPort<ConcurrentHashMap<String, String>, Map<String, String>> ioPort, final Character splitWith) {
        this.values = new ConcurrentHashMap<String, List<String>>();
        this.log = new Debugger(PropertiesMapper.class.getName());
        this.log = new Debugger(PropertiesMapper.class.getName(), ioPort.getFile().getName());
        this.splitWith = splitWith;
        this.ioPort = ioPort;
        this.map = this.ioPort.read();
        for (final Map.Entry<String, String> entry : this.map.entrySet()) {
            final String value = entry.getValue();
            Utils.safePut((ConcurrentHashMap<String, ArrayList>) this.values, entry.getKey(), new ArrayList(Arrays.asList(value.split((!value.contains(splitWith.toString()) && value.length() > 2) ? ";" : splitWith.toString()))));
            if (!value.contains(splitWith.toString())) {
                this.write();
            }
        }
    }

    public SelectedPropertiesMapper select(final Object key) {
        return new SelectedPropertiesMapper(this.ioPort, this.splitWith, key);
    }

    public Character getSplitWith() {
        return this.splitWith;
    }

    public PropertiesMapper add(final Object toKey, final Object add) {
        if (!this.values.containsKey(toKey.toString())) {
            this.values.put(toKey.toString(), new ArrayList<String>());
        }
        this.values.get(toKey.toString()).add(add.toString());
        return this;
    }

    public String get(final Object fromKey, final int index) {
        return this.values.get(fromKey.toString()).get(index);
    }

    public String softGet(final Object key, final int index, final Object valueIfAbsent) {
        if (!this.values.containsKey(key.toString())) {
            this.values.put(key.toString(), new ArrayList<String>());
            this.values.get(key.toString()).add(index, valueIfAbsent.toString());
            return valueIfAbsent.toString();
        }
        if (this.values.get(key.toString()).size() > index) {
            return this.values.get(key.toString()).get(index);
        }
        this.values.get(key.toString()).add(index, valueIfAbsent.toString());
        return valueIfAbsent.toString();
    }

    public String set(final Object key, final int index, final Object item) {
        if (this.values.containsKey(key.toString())) {
            if (this.values.get(key.toString()).size() <= index) {
                this.values.get(key.toString()).add(item.toString());
            } else {
                this.values.get(key.toString()).set(index, item.toString());
            }
        } else {
            final ArrayList<String> list = new ArrayList<String>();
            list.add(item.toString());
            this.values.put(key.toString(), list);
        }
        return item.toString();
    }

    public ArrayList<String> set(final Object key, final ArrayList<String> newValues) {
        if (this.values.containsKey(key.toString())) {
            this.values.replace(key.toString(), newValues);
        } else {
            this.values.put(key.toString(), newValues);
        }
        return newValues;
    }

    public List<String> getAll(final Object fromKey) {
        List<String> val;
        if (this.values.containsKey(fromKey.toString())) {
            val = this.values.get(fromKey.toString());
        } else {
            val = new ArrayList<String>();
            this.values.put(fromKey.toString(), val);
        }
        return val;
    }

    public PropertiesMapper clear(final Object key) {
        this.values.get(key.toString()).clear();
        return this;
    }

    public boolean containsKey(final Object key) {
        return this.values.containsKey(key.toString());
    }

    public int size(final Object key) {
        return this.values.get(key.toString()).size();
    }

    public void remove(final Object key, final int index) {
        this.values.get(key.toString()).remove(index);
    }

    public void addAll(final Object key, final ArrayList<Object> values) {
        final String t;
        final List<String> orDefault;
        values.forEach(v -> {
            t = v.toString();
            orDefault = this.values.getOrDefault(key.toString(), new ArrayList<String>());
            orDefault.add(t);
            this.values.put(key.toString(), orDefault);
        });
    }

    public boolean containsValue(final Object key, final Object value) {
        return this.values != null && this.values.containsKey(key.toString()) && this.values.get(key.toString()).contains(value.toString());
    }

    public boolean containsValue(final Object value) {
        if (this.values == null) {
            return false;
        }
        for (final Map.Entry<String, List<String>> entry : this.values.entrySet()) {
            if (entry.getValue().contains(value.toString())) {
                return true;
            }
        }
        return false;
    }

    public PropertiesMapper removeValue(final Object fromKey, final Object value) {
        if (this.values.containsKey(fromKey.toString())) {
            final List<String> val = this.values.get(fromKey.toString()).stream().filter(e -> !e.equals(value.toString())).collect(Collectors.toList());
            this.values.put(fromKey.toString(), val);
            if (this.map.get(fromKey.toString()).isEmpty()) {
                this.map.remove(fromKey.toString());
            }
            this.write();
        }
        return this;
    }

    public void removeKey(final Object key) {
        this.values.remove(key.toString());
        this.map.remove(key.toString());
    }

    public int mapSize() {
        return this.values.size();
    }

    public Set<Map.Entry<String, List<String>>> entrySet() {
        return this.values.entrySet();
    }

    public void write() {
        final String s;
        this.values.forEach((key, value) -> s = this.map.put(key, value.stream().collect(CustomCollectors.toConcatenatedString(this.splitWith))));
        this.ioPort.write(this.map);
    }

    public ConcurrentHashMap<String, String> getMap() {
        return this.map;
    }

    public ConcurrentHashMap<String, List<String>> getValues() {
        return this.values;
    }

    public void clearAll() {
        this.values.clear();
        this.map.clear();
        this.ioPort.write(this.map);
    }
}
