package org.comroid.javacord.util.test.model

import org.comroid.javacord.util.model.PrioritySupplier
import org.junit.Assert
import org.junit.Test

class PrioritySupplierTest {
    @Test
    void testNotNull() {
        def value = new PrioritySupplier<String>(false, "fall")
                .possible("back")
                .possible(null)
                .get()
        def nil = new PrioritySupplier<String>(false, "fall")
                .possible("back")
                .possible(null)
                .get(true)

        Assert.assertEquals("back", value)
        Assert.assertEquals(null, nil)
    }

    @Test
    void testNullable() {
        def nil = new PrioritySupplier<String>(true, "fall")
                .possible("back")
                .possible(null)
                .get()
        def value = new PrioritySupplier<String>(true, "fall")
                .possible("back")
                .possible(null)
                .get(false)

        Assert.assertEquals(null, nil)
        Assert.assertEquals("back", value)
    }

    @Test
    void testFallback() {
        def nil = new PrioritySupplier<String>(true, "fall")
                .possible(null)
                .get()
        def value = new PrioritySupplier<String>(true, "fall")
                .possible(null)
                .get(false)

        Assert.assertEquals(null, nil)
        Assert.assertEquals("fall", value)
    }
}
