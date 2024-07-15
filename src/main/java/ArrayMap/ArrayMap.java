package ArrayMap;

import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class ArrayMap<V> extends AbstractMap<Integer, V> {
    private final V[] array;

    @SuppressWarnings("unchecked")
    public ArrayMap(int size) {
        array = (V[]) new Object[size];
    }

    @Override
    public V get(Object key) {
        if (key instanceof Integer) {
            int index = (Integer) key;
            if (index >= 0 && index < array.length) {
                return array[index];
            }
        }
        return null;
    }

    @Override
    public V put(Integer key, V value) {
        int index = key;
        if (index >= 0 && index < array.length) {
            V oldValue = array[index];
            array[index] = value;
            return oldValue;
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + array.length);
    }

    @Override
    public Set<Map.Entry<Integer, V>> entrySet() {
        return new ArrayMapSet();
    }

    private class ArrayMapSet extends AbstractSet<Map.Entry<Integer, V>> {
        @Override
        public Iterator<Map.Entry<Integer, V>> iterator() {
            return new Iterator<>() {
                private int currentIndex = 0;

                @Override
                public boolean hasNext() {
                    return currentIndex < array.length;
                }

                @Override
                public Map.Entry<Integer, V> next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final int index = currentIndex++;
                    return new AbstractMap.SimpleEntry<>(index, array[index]);
                }
            };
        }

        @Override
        public int size() {
            return array.length;
        }
    }
}