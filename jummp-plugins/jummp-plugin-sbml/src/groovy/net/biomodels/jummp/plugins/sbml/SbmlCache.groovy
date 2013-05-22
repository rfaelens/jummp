package net.biomodels.jummp.plugins.sbml

import org.sbml.jsbml.SBMLDocument
import net.biomodels.jummp.core.model.ModelVersionTransportCommand
import java.util.AbstractMap.SimpleEntry
import java.util.concurrent.locks.ReentrantLock

/**
 * @short A last recently used cache for SBMLDocument.
 *
 * This class provides a last recently used cache for SBMLDocuments. Internally it uses a
 * LinkedHashMap mapping ids to the SBMLDocument. It is meant to keep the SBML documents
 * belonging to a specific Revision in memory. Because of that the actual class uses
 * RevisionTransportCommands as the key.
 *
 * All access to the internal cache is protected by a reentrant lock, so that the cache can
 * be accessed from multiple threads.
 *
 * @autor Martin Gräßlin <m.graesslin@dkfz.de> 
 */
class SbmlCache<K extends ModelVersionTransportCommand, V extends SBMLDocument> implements Map<K, V> {

    /**
     * Internal cache extending LinkedHashMap with the contract of a last recently used cache.
     */
    private class InternalCache<K extends Long, V extends SBMLDocument> extends LinkedHashMap<K, V> {
        private Integer maxSize
        InternalCache(int maxSize) {
            super(0, 0.75, true)
            this.maxSize = maxSize
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<? super Long, ? extends SBMLDocument> eldest) {
            return (size() > maxSize)
        }
    }

    /**
     * The internal cache.
     */
    private InternalCache<Long, SBMLDocument> cache
    /**
     * Lock to protect access to the cache.
     */
    private static final ReentrantLock lock = new ReentrantLock()

    /**
     * Constructor for the Cache taking the maximum cache size as argument.
     * @param maxSize
     */
    public SbmlCache(int maxSize) {
        this.cache = new InternalCache<Long, SBMLDocument>(maxSize)
    }

    int size() {
        int size = 0
        lock.lock()
        try {
            size = cache.size()
        } finally {
            lock.unlock()
        }
        return size
    }

    boolean isEmpty() {
        boolean empty = false
        lock.lock()
        try {
            empty = cache.isEmpty()
        } finally {
            lock.unlock()
        }
        return empty
    }

    boolean containsKey(Object key) {
        if (key instanceof ModelVersionTransportCommand) {
            boolean contains = false
            lock.lock()
            try {
                contains = cache.containsKey(key.id)
            } finally {
                lock.unlock()
            }
            return contains
        } else {
            return false
        }
    }

    boolean containsValue(Object value) {
        if (value instanceof SBMLDocument) {
            boolean contains = false
            lock.lock()
            try {
                contains = cache.containsValue(value)
            } finally {
                lock.unlock()
            }
            return contains
        } else {
            return false
        }
    }

    V get(Object key) {
        if (key instanceof ModelVersionTransportCommand) {
            V value = null
            lock.lock()
            try {
                value = (V)cache.get(key.id)
            } finally {
                lock.unlock()
            }
        } else {
            return null
        }
    }

    V put(K key, V value) {
        V retValue = null
        lock.lock()
        try {
            retValue = (V)cache.put(key.id, value)
        } finally {
            lock.unlock()
        }
        return retValue
    }

    V remove(Object key) {
        if (key instanceof ModelVersionTransportCommand) {
            V value = null
            lock.lock()
            try {
                value = (V)cache.remove(key.id)
            } finally {
                lock.unlock()
            }
            return value
        } else {
            return null;
        }
    }

    void putAll(Map<? extends K, ? extends V> m) {
        Map<Long, V> entries = [:]
        m.each{ k, v ->
            entries.put(k.id, v)
        }
        lock.lock()
        try {
            cache.putAll(entries)
        } finally {
            lock.unlock()
        }
    }

    void clear() {
        lock.lock()
        try {
            cache.clear()
        } finally {
            lock.unlock()
        }
    }

    Set<K> keySet() {
        Set<? extends Long> keys = []
        lock.lock()
        try {
            keys = cache.keySet()
        } finally {
            lock.unlock()
        }
        Set<K> versions = []
        keys.each {
            versions.add((K)(new ModelVersionTransportCommand(id: it)))
        }
        return versions
    }

    Collection<SBMLDocument> values() {
        Collection<SBMLDocument> retVals = []
        lock.lock()
        try {
            retVals = cache.values()
        } finally {
            lock.unlock()
        }
        return retVals
    }

    Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> entries = []
        lock.lock()
        try {
            cache.entrySet().each {
                entries.add(new SimpleEntry<ModelVersionTransportCommand, SBMLDocument>(new ModelVersionTransportCommand(id: it.key), it.value))
            }
        } finally {
            lock.unlock()
        }
        return entries
    }
}
