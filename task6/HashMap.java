import java.util.*;
import java.util.Map.Entry;

/**
 * This class implements a hash table, which maps keys to values.
 * This implementantion permits <tt>null</tt> values and the <tt>null</tt> key.
 */
public class HashMap<K, V> {
    /**
     * The default initial capacity, used when none specified in constructor.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity,.
     */
    public static final int MAXIMUM_CAPACITY = 1073741824;

    /**
     * The load factor, used when none specified in constructor.
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75F;

    private Entry<K, V>[] table;

    private int size;
    private int threshold;
    private final float loadFactor;

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param  capacity the initial capacity
     * @param  loadFactor the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
    public HashMap(int capacity, float loadFactor) {
        if (capacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + capacity);
        if (capacity > MAXIMUM_CAPACITY)
            capacity = MAXIMUM_CAPACITY;
        if ((loadFactor <= 0.0F) || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

        this.size = 0;
        this.table = new Entry[capacity];
        this.loadFactor = loadFactor;
        this.threshold = (int) (capacity * loadFactor);
    }

    /**
     * Construct an empty <tt>HashMap</tt> with the specified initial
     * capacity and the default load factor (0.75).
     *
     * @param capacity the initial capacity
     */
    public HashMap(int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt></tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public V put(K key, V value) {
        int h = hash1(key);
        for (int i = 0; i < table.length; i++) {
            int idx = h % table.length;
            if (table[idx] == null) {
                table[idx] = new HashEntry<>(key, value);
                break;
            } else if (key.equals(table[idx].getKey())) {
                V oldValue = table[idx].getValue();
                table[idx].setValue(value);
                return oldValue;
            }
            h += i * hash2(key);
        }
        if (++size > threshold)
            resize();
        return null;
    }


    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     */
    public V get(Object key) {
        int h = hash1(key);
        for (int i = 0; i < table.length; i++) {
            int idx = h % table.length;
            if (table[idx] != null) {
                if (key.equals(table[idx].getKey()))
                    return table[idx].getValue();
            }
            h += i * hash2(key);
        }
        return null;
    }

    /**
     * Remove the mapping for the specified key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public boolean remove(Object key) {
        int h = hash1(key);
        for (int i = 0; i < table.length; i++) {
            int idx = h % table.length;
            if (table[idx] != null && key.equals(table[idx].getKey())) {
                table[idx] = null;
                return true;
            }
            h += i * hash2(key);
        }
        return false;
    }

    /**
     * Return the number of key-value mappings in this map.
     * @return the number of key-value mappings in this map
     */
    public int size() { return size; }

    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> set = new HashSet<>(size);
        for (Entry<K, V> e : table)
            if (e != null)
                set.add(e);
        return set;
    }

    private void resize() {
        if (table.length >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        int newCapacity = table.length * 2;
        threshold = (int) (newCapacity * loadFactor);
        Entry<K, V>[] oldTable = table;
        table = new Entry[newCapacity];

        for (int i = 0; i < oldTable.length; i++) {
            if (oldTable[i] == null)
                continue;
            K key = oldTable[i].getKey();
            V value = oldTable[i].getValue();

            int h = hash1(key);
            for (int j = 0; j < table.length; j++) {
                int idx = h % table.length;
                if (table[idx] == null) {
                    table[idx] = new HashEntry<>(key, value);
                    break;
                }
                h += j * hash2(key);
            }
            oldTable[i] = null;
        }
    }

    private static int hash1(Object key) {
        return key.hashCode() * 31;
    }

    private static int hash2(Object key) {
        return 1 + key.hashCode() * 63;
    }

    private static class HashEntry<K, V> implements Entry<K, V> {
        private final K key;
        private V value;

        public HashEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() { return key; }

        public V getValue() { return value; }

        public V setValue(V value) {
            V prev = this.value;
            this.value = value;
            return prev;
        }
    }

    public static void main(String[] args) {
        HashMap<Integer, String> hashMap = new HashMap<>(4);
        for (int i = 0; i < 10; i++)
            hashMap.put(i, "i: " + i);
        for (Entry<Integer, String> e : hashMap.entrySet())
            System.out.println(e.getValue());
        hashMap.remove(4);
        System.out.println("after remove/");
        for (int i = 0; i < 10; i++)
            System.out.println(hashMap.get(i));
    }
}
