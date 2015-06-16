import java.util.*;

public class HashMap<K, V> {
    public static final int DEFAULT_INITIAL_CAPACITY = 16;
    public static final int MAXIMUM_CAPACITY = 1073741824;
    public static final float DEFAULT_LOAD_FACTOR = 0.75F;

    private Map.Entry<K, V>[] table;
    private int size;

    private final int threshold;
    private final float loadFactor;

    public HashMap(int capacity, float loadFactor) {
        this.size = 0;
        this.table = new HashEntry[capacity];
        this.loadFactor = loadFactor;
        this.threshold = (int) (table.length * loadFactor);
    }

    public HashMap(int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public void add(K key, V value) {
        int h = hash1(key);
        for (int i = 0; i < table.length; i++) {
            int idx = indexFor(h, table.length);
            if (table[idx] == null) {
                table[idx] = new HashEntry<>(key, value);
                return;
            }
            else if (key.equals(table[idx].getKey())) {
                table[idx].setValue(value);
                return;
            }
            h += i * hash2(key);
        }
        //resize();
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


    private static int hash1(Object key) {
        return key.hashCode() % 13;
    }

    private static int hash2(Object key) {
        return 1 + key.hashCode() % 11;
    }

    private static int indexFor(int h, int length) {
        return h & (length - 1);
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
        HashMap<Integer, String> hm = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            hm.add(i, "H: " + i);
        }

        for (int i = 0; i < 10; i++) {
            System.out.println(hm.get(i));
        }

        hm.remove(4);

        for (int i = 0; i < 10; i++) {
            System.out.println(hm.get(i));
        }
    }
}
