package browser.data_structure;

public class Hashtable<K, V> {
    private static class HashNode<K, V> {
        K key;
        V value;
        HashNode<K, V> next;

        public HashNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private LinkedList<HashNode<K, V>>[] chainArray;
    private int M; // Tamaño de la tabla hash
    private int size;
    private static final double LOAD_FACTOR = 0.85;

    public Hashtable() {
        this(11); // Tamaño inicial por defecto
    }

    public Hashtable(int initialCapacity) {
        M = initialCapacity;
        chainArray = new LinkedList[M];
        for (int i = 0; i < M; i++) {
            chainArray[i] = new LinkedList<>();
        }
        size = 0;
    }

    private int hash(K key) {
        return Math.abs(key.hashCode() % M);
    }

    public void put(K key, V value) {
        if ((1.0 * size) / M >= LOAD_FACTOR) {
            resize();
        }

        int index = hash(key);
        for (HashNode<K, V> node : chainArray[index]) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }
        chainArray[index].add(new HashNode<>(key, value));
        size++;
    }

    public void putAll(Hashtable<K, V> hashtable) {
        for (LinkedList<HashNode<K, V>> bucket : hashtable.chainArray) {
            for (HashNode<K, V> node : bucket) {
                put(node.key, node.value);
            }
        }
    }

    public V get(K key) {
        int index = hash(key);
        for (HashNode<K, V> node : chainArray[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    public V remove(K key) {
        int index = hash(key);
        HashNode<K, V> toRemove = null;
        for (HashNode<K, V> node : chainArray[index]) {
            if (node.key.equals(key)) {
                toRemove = node;
                break;
            }
        }
        if (toRemove != null) {
            chainArray[index].remove(toRemove);
            size--;
            return toRemove.value;
        }
        return null;
    }

    public boolean containsKey(K key) {
        int index = hash(key);
        for (HashNode<K, V> node : chainArray[index]) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsValue(V value) {
        for (LinkedList<HashNode<K, V>> bucket : chainArray) {
            for (HashNode<K, V> node : bucket) {
                if (node.value.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    // obtener claves
    public LinkedList<K> keySet() {
        LinkedList<K> keys = new LinkedList<>();
        for (LinkedList<HashNode<K, V>> bucket : chainArray) {
            for (HashNode<K, V> node : bucket) {
                keys.add(node.key);
            }
        }
        return keys;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    private void resize() {
        LinkedList<HashNode<K, V>>[] oldChainArray = chainArray;
        M = 2 * M;
        chainArray = new LinkedList[M];
        for (int i = 0; i < M; i++) {
            chainArray[i] = new LinkedList<>();
        }
        size = 0;

        for (LinkedList<HashNode<K, V>> bucket : oldChainArray) {
            for (HashNode<K, V> node : bucket) {
                put(node.key, node.value);
            }
        }
    }

    public void clear() {
        for (int i = 0; i < M; i++) {
            chainArray[i].clear();
        }
        size = 0;
    }
}