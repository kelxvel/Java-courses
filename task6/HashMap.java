import java.util.*;
import java.util.Map.Entry;

/**
 * Created by kelxvel on 14.06.15.
 */
public class HashMap<K, V> {
    public static final int DEFAULT_INITIAL_CAPACITY = 16;
    public static final int MAXIMUM_CAPACITY = 1073741824;
    public static final float DEFAULT_LOAD_FACTOR = 0.75F;

    private Entry<K, V>[] table;

    private int size;
    private int threshold;
    private final float loadFactor;

    public HashMap(int capacity, float loadFactor) {
        if (capacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + capacity);
        if (capacity > MAXIMUM_CAPACITY)
            capacity = MAXIMUM_CAPACITY;
        if ((loadFactor <= 0.0F) || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

        this.size = 0;
        this.table = new HashEntry[capacity];
        this.loadFactor = loadFactor;
        this.threshold = (int) (capacity * loadFactor);
    }

    public HashMap(int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public V put(K key, V value) {
        int h = hash1(key);
        for (int i = 0; i < table.length; i++) {
            int idx = indexFor(h, table.length);
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

    public V get(Object key) {
        int h = hash1(key);
        for (int i = 0; i < table.length; i++) {
            int idx = indexFor(h, table.length);
            if (table[idx] != null) {
                if (key.equals(table[idx].getKey()))
                    return table[idx].getValue();
            }
            h += i * hash2(key);
        }
        return null;
    }

    public boolean remove(Object key) {
        int h = hash1(key);
        for (int i = 0; i < table.length; i++) {
            int idx = indexFor(h, table.length);
            if (table[idx] != null && key.equals(table[idx].getKey())) {
                table[idx] = null;
                return true;
            }
            h += i * hash2(key);
        }
        return false;
    }

    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> set = new HashSet<>(size);
        for (int i = 0; i < table.length; i++)
            if (table[i] != null)
                set.add(table[i]);
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
        Entry<K, V>[] newTable = new Entry[newCapacity];
        table = newTable;

        for (int i = 0; i < oldTable.length; i++) {
            if (oldTable[i] == null)
                continue;
            K key = oldTable[i].getKey();
            V value = oldTable[i].getValue();

            int h = hash1(key);
            for (int j = 0; j < table.length; j++) {
                int idx = indexFor(h, table.length);
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

    private static int indexFor(int h, int length) {
        return h % length;
    }

    private static class HashEntry<K, V> implements Map.Entry<K, V> {
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
        HashMap<Integer, String> hm = new HashMap<>(4);

        for (int i = 0; i < 10; i++) {
            hm.put(i, "i: " + i);
        }

        for (Entry<Integer, String> e : hm.entrySet()) {
            //if (e == null) continue;
            System.out.println(e.getValue());
        }

        hm.remove(4);

        System.out.println("after remove/");
        for (int i = 0; i < 10; i++) {
            System.out.println(hm.get(i));
        }
    }
}
