// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.tools;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import de.kaleidox.util.Bot;

public class Debugger {
    private static Debugger log;
    private final SimpleDateFormat sdf;
    private String title;
    private String subclass;
    private boolean isSubclass;
    private StringBuilder sb;

    static {
        Debugger.log = new Debugger("Debugger");
    }

    public Debugger(final String title) {
        this.sdf = new SimpleDateFormat("HH:mm:ss");
        this.isSubclass = false;
        this.sb = new StringBuilder();
        this.title = title;
    }

    public Debugger(final String title, final String subclass) {
        this.sdf = new SimpleDateFormat("HH:mm:ss");
        this.isSubclass = false;
        this.sb = new StringBuilder();
        this.title = title;
        this.subclass = subclass;
        this.isSubclass = true;
    }

    public void speak() {
        this.put("### --- DEBUGGING --- ###", true);
    }

    public Boolean put(final Object t, final boolean isDebug) {
        return this.put(t.toString(), isDebug);
    }

    public Boolean put(final Object t) {
        try {
            return this.put(t.toString());
        } catch (NullPointerException e) {
            return this.put("Tried to output NULL with cause: " + e.getCause());
        }
    }

    public Boolean put(final String method, final String message, final boolean isDebug) {
        if (!isDebug) {
            return this.put("[" + method + "] " + message);
        }
        if (Bot.isTesting()) {
            return this.put("[" + method + "] [Debug] " + message);
        }
        return false;
    }

    public Boolean put(final String message, final boolean isDebug) {
        if (!isDebug) {
            return this.put(message);
        }
        if (Bot.isTesting()) {
            return this.put("[Debug] " + message);
        }
        return this.put("[Debug] " + message);
    }

    public Boolean put(final String message) {
        this.clear();
        this.sb.append("[");
        this.putTime();
        this.sb.append("|");
        this.sb.append(this.title);
        this.sb.append(this.isSubclass ? (":" + this.subclass) : "");
        this.sb.append("] ");
        this.sb.append(message);
        return this.send();
    }

    private Boolean send() {
        Boolean give;
        try {
            System.out.println(this.sb.toString());
        } catch (NullPointerException e) {
            give = false;
        } finally {
            give = true;
        }
        return give;
    }

    private void clear() {
        this.sb.delete(0, this.sb.length());
    }

    private String getTime() {
        return this.sdf.format(new Timestamp(System.currentTimeMillis()));
    }

    private void putTime() {
        this.sb.append(this.getTime());
    }

    public static void print() {
        System.out.println("### --- DEBUGGING --- ###");
    }
}
