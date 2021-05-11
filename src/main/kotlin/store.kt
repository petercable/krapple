import java.util.*

open class StoreException: Exception()
class KeyNotFound(val key: String): StoreException()
class NoTransaction: StoreException()

sealed class Element
data class Value(val value: String): Element()
object Deleted: Element()

class Store {
    // the store tracks a stack of Elements per key
    // the current value is always the first element on the stack
    // so reads never need to walk the stack
    private val store: MutableMap<String, Deque<Element>> = mutableMapOf()
    // the stack tracks a set of keys per tx level
    // so that we don't have to search all keys on abort/commit
    private val stack: Deque<MutableSet<String>> = LinkedList()

    val level: Int
        get() = stack.size

    init {
        start()
    }

    fun read(key: String): String {
        // if the key is present in the dictionary and is Value, return it
        // otherwise throw KeyNotFound
        return when (val element = store[key]?.peek()) {
            is Value -> element.value
            else -> throw KeyNotFound(key)
        }
    }

    fun write(key: String, value: String) {
        // get the stack for this key (creating it if necessary)
        val state = store.computeIfAbsent(key) { LinkedList() }

        // check if key already in tx
        // if so, remove it from the per-key stack in the store
        val tx = stack.peek()
        if (tx.contains(key)) {
            state.pop()
        }

        // push this value onto the stack for this key
        state.push(Value(value))
        // add this key to the tracked set for this tx level
        stack.peek().add(key)
    }

    fun delete(key: String) {
        if (!store.containsKey(key)) {
            throw KeyNotFound(key)
        }
        // check if key already in tx
        // if so, remove it
        val tx = stack.peek()
        if (tx.contains(key)) {
            store[key]!!.pop()
        } else {
            tx.add(key)
        }
        when (level) {
            1 -> {}
            else -> {
                // push delete onto the stack for this key
                store[key]!!.push(Deleted)
            }
        }
    }

    fun start() = stack.push(mutableSetOf())

    fun commit() {
        val txn = popTx()
        val parent = stack.peek()
        txn.forEach { key ->
            val state = store[key]!!
            val element = state.pop()
            if (parent.contains(key)) {
                state.pop()
            } else {
                parent.add(key)
            }
            when {
                element is Deleted && level > 1 -> state.push(Deleted)
                element is Deleted -> {}
                element is Value -> state.push(element)
            }
        }
    }

    fun abort() = popTx().forEach { key -> store[key]!!.pop() }

    private fun popTx(): Set<String> {
        if (level == 1) throw NoTransaction()
        return stack.pop()
    }
}