package de.kaleidox.util.interfaces;

import java.io.IOException;

/**
 * Defines an object to require termination.
 * During termination, the class may throw IOExceptions.
 */
public interface Terminatable {
    /**
     * The method to be called to terminate the object.
     *
     * @throws IOException Exceptions that happen during IO operations on termination.
     */
    void terminate() throws IOException;
}
