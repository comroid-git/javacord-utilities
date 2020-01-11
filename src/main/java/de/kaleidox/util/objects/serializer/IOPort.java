// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects.serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.kaleidox.util.tools.Debugger;

public class IOPort<R, W> {
    private static Debugger log;
    private File file;
    private Supplier<R> reader;
    private Consumer<W> writer;

    static {
        IOPort.log = new Debugger(IOPort.class.getName());
    }

    public IOPort(final File file, final Supplier<R> reader, final Consumer<W> writer) {
        this.file = file;
        this.reader = reader;
        this.writer = writer;
    }

    public File getFile() {
        return this.file;
    }

    public R read() {
        return this.reader.get();
    }

    public Collection<String> readAsCollection(final Collection<String> supplier, final Character splitWith) {
        return new ArrayList<String>(Arrays.asList(this.reader.get().toString().split(splitWith.toString())));
    }

    public void write(final W item) {
        this.writer.accept(item);
    }

    public static IOPort<ConcurrentHashMap<String, String>, Map<String, String>> mapPort(final File file) {
        final Properties props;
        final ConcurrentHashMap<String, String> map;
        final String s;
        final Properties props2;
        final Iterator<Map.Entry<String, String>> iterator;
        Map.Entry<String, String> entry;
        return new IOPort<ConcurrentHashMap<String, String>, Map<String, String>>(file, () -> {
            props = new Properties();
            map = new ConcurrentHashMap<String, String>();
            try {
                props.load(new FileInputStream(file.getPath()));
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            props.forEach((key, value) -> s = map.put(key.toString(), value.toString()));
            return map;
        }, item -> {
            props2 = new Properties();
            item.entrySet().iterator();
            while (iterator.hasNext()) {
                entry = iterator.next();
                props2.put(entry.getKey(), entry.getValue());
            }
            try {
                props2.store(new FileOutputStream(file.getPath()), null);
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        });
    }
}
