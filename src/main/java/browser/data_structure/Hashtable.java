package browser.data_structure;

/**
 * La clase Hashtable representa una tabla hash genérica que utiliza encadenamiento para manejar colisiones.
 * @param <K> el tipo de las claves en la tabla hash.
 * @param <V> el tipo de los valores en la tabla hash.
 */
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

    /**
     * Constructor que inicializa la tabla hash con un tamaño por defecto.
     */
    public Hashtable() {
        this(11); // Tamaño inicial por defecto
    }

    /**
     * Constructor que inicializa la tabla hash con un tamaño inicial especificado.
     * @param initialCapacity el tamaño inicial de la tabla hash.
     */
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

    /**
     * Inserta una clave y un valor en la tabla hash.
     * @param key la clave a insertar.
     * @param value el valor a insertar.
     */
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

    /**
     * Inserta todas las claves y valores de otra tabla hash en esta tabla hash.
     * @param hashtable la tabla hash cuyos elementos se agregarán.
     */
    public void putAll(Hashtable<K, V> hashtable) {
        for (LinkedList<HashNode<K, V>> bucket : hashtable.chainArray) {
            for (HashNode<K, V> node : bucket) {
                put(node.key, node.value);
            }
        }
    }

    /**
     * Obtiene el valor asociado a una clave en la tabla hash.
     * @param key la clave cuyo valor se desea obtener.
     * @return el valor asociado a la clave, o null si la clave no está presente.
     */
    public V get(K key) {
        int index = hash(key);
        for (HashNode<K, V> node : chainArray[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    /**
     * Elimina una clave y su valor asociado de la tabla hash.
     * @param key la clave a eliminar.
     * @return el valor asociado a la clave eliminada, o null si la clave no está presente.
     */
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

    /**
     * Verifica si una clave está presente en la tabla hash.
     * @param key la clave a verificar.
     * @return true si la clave está presente, false en caso contrario.
     */
    public boolean containsKey(K key) {
        int index = hash(key);
        for (HashNode<K, V> node : chainArray[index]) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si un valor está presente en la tabla hash.
     * @param value el valor a verificar.
     * @return true si el valor está presente, false en caso contrario.
     */
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

    /**
     * Obtiene un conjunto de todas las claves en la tabla hash.
     * @return una lista enlazada de todas las claves en la tabla hash.
     */
    public LinkedList<K> keySet() {
        LinkedList<K> keys = new LinkedList<>();
        for (LinkedList<HashNode<K, V>> bucket : chainArray) {
            for (HashNode<K, V> node : bucket) {
                keys.add(node.key);
            }
        }
        return keys;
    }

    /**
     * Verifica si la tabla hash está vacía.
     * @return true si la tabla hash está vacía, false en caso contrario.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Obtiene el tamaño de la tabla hash.
     * @return el número de elementos en la tabla hash.
     */
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

    /**
     * Elimina todos los elementos de la tabla hash.
     */
    public void clear() {
        for (int i = 0; i < M; i++) {
            chainArray[i].clear();
        }
        size = 0;
    }
}