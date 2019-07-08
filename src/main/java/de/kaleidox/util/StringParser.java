package de.kaleidox.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum StringParser {
    INSTANCE;

    /**
     * <table>
     * <tr>
     * <th>Value</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>Any {@code int} that is {@code &gt;= 0}<br></td>
     * <td>Parses all following arguments as one single {@link String}.
     * <br>The given {@code int} defines the index of the starting argument.</td>
     * </tr>
     * <tr>
     * <td>{@code -1}</td>
     * <td>Default {@link String} parsing.
     * <br>Uses {@code "} marks to determine where an intended {@link String} starts and ends.</td>
     * </tr>
     * <tr>
     * <td>{@code -2}</td>
     * <td>Disables {@link String} parsing.</td>
     * </tr>
     * </table>
     *
     * @param option
     * @param delimiter
     * @param args
     * @return
     */
    public String[] parseStrings(int option, String delimiter, String... args) {
        String content = String.join(delimiter, args);

        List<String> yields = new ArrayList<>();
        yields.add("");

        boolean inString = false;
        int i = 0, y = 0, s = -1;
        char c = 0, p;

        while (i < content.length()) {
            p = c;
            c = content.charAt(i);

            switch (c) {
                case '"':
                    // if not in string & starter is not escaped
                    if (!inString && p != '\\') {
                        // if string starts after a space
                        if (p == ' ') {
                            // start string
                            yields.add("");
                            s = y++;
                            inString = true;
                        } else {
                            // escape "
                            yields.set(y, yields.get(y) + c);
                        }
                        // if in string & ender is not escaped
                    } else if (inString && p != '\\') {
                        // if there are more chars
                        if (content.length() < i + 1) {
                            // if next char is space
                            if (content.length() < i + 1 && content.charAt(i + 1) == ' ') {
                                // end string
                                yields.add("");
                                s = y++;
                                inString = false;
                            } else {
                                // escape "
                                yields.set(y, yields.get(y) + c);
                            }
                        } else {
                            // end string
                            inString = false;
                        }
                        // if " was escaped
                    } else {
                        // append to string
                        yields.set(y, yields.get(y) + c);
                    }
                    break;
                case ' ':
                    if (inString) {
                        // append to string
                        yields.set(y, yields.get(y) + c);
                    } else {
                        // start new item
                        yields.add("");
                        s = y++;
                    }
                    break;
                case '\\':
                    // never include escaping character
                    break;
                default:
                    // append to string
                    yields.set(y, yields.get(y) + c);
                    break;
            }

            i++;
        }

        if (inString) { // if still in string -> join split string together
            int prevSize = yields.size();
            new ArrayList<>(yields)
                    .stream()
                    .skip(s)
                    .flatMap(str -> Arrays.stream(str.split(" ")))
                    .forEachOrdered(yields::add);
            if (prevSize >= s + 1) yields.subList(s + 1, prevSize + 1).clear();
            yields.set(s + 1, '"' + yields.get(s + 1));
        }

        yields.removeIf(String::isEmpty);

        return yields.toArray(new String[0]);
    }
}
