import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StoreTest {

    private val store = Store()

    @Test
    fun `test write then read`() {
        store.write("foo", "bar")
        assertEquals("bar", store.read("foo"))
    }

    @Test
    fun `test read key does not exist`() {
        assertThrows(KeyNotFound::class.java) { store.read("foo") }
    }

    @Test
    fun `test write then read then delete then read`() {
        store.write("foo", "bar")
        assertEquals("bar", store.read("foo"))
        store.delete("foo")
        assertThrows(KeyNotFound::class.java) { store.read("foo") }
    }

    @Test
    fun `test commit and abort outside transaction`() {
        assertThrows(NoTransaction::class.java) { store.commit() }
        assertThrows(NoTransaction::class.java) { store.abort() }
    }

    @Test
    fun `test transaction abort`() {
        assertThrows(NoTransaction::class.java) { store.commit() }
        store.write("foo", "bar")
        store.start()
        assertEquals("bar", store.read("foo"))
        store.write("foo", "baz")
        assertEquals("baz", store.read("foo"))
        store.abort()
        assertEquals("bar", store.read("foo"))
    }

    @Test
    fun `test transaction commit`() {
        store.write("foo", "bar")
        store.start()
        assertEquals("bar", store.read("foo"))
        store.write("foo", "baz")
        assertEquals("baz", store.read("foo"))
        store.commit()
        assertEquals("baz", store.read("foo"))
    }

    @Test
    fun `test nested transaction`() {
        store.write("foo", "bar")

        // LET THE NESTING BEGIN
        repeat(1000000) {
            store.start()
        }

        assertEquals("bar", store.read("foo"))
        store.delete("foo")
        assertThrows(KeyNotFound::class.java) { store.read("foo") }

        repeat(999999) {
            store.commit()
            assertThrows(KeyNotFound::class.java) { store.read("foo") }
        }

        store.abort()
        assertEquals("bar", store.read("foo"))
    }
}