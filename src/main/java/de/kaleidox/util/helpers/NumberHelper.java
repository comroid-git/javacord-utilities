package de.kaleidox.util.helpers;

public class NumberHelper extends NullHelper {
    public static String pluralize(String pluralAddition, int amount) {
        return amount == 1 ? "" : pluralAddition;
    }
}
