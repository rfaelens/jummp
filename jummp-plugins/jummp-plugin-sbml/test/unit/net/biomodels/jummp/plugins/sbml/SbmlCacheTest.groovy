package net.biomodels.jummp.plugins.sbml

import grails.test.GrailsUnitTestCase
import net.biomodels.jummp.core.model.RevisionTransportCommand
import org.sbml.jsbml.SBMLDocument
import java.util.Map.Entry

/**
 * Created by IntelliJ IDEA.
 * User: graessli
 * Date: May 2, 2011
 * Time: 11:48:22 AM
 * To change this template use File | Settings | File Templates.
 */
class SbmlCacheTest extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testMaxSize() {
        SbmlCache<RevisionTransportCommand, SBMLDocument> cache = new SbmlCache<RevisionTransportCommand, SBMLDocument>(1)
        assertTrue(cache.isEmpty())
        assertEquals(0, cache.size())
        cache.put(new RevisionTransportCommand(id: 1), new SBMLDocument())
        assertEquals(1, cache.size())
        SBMLDocument doc = new SBMLDocument(3, 1)
        cache.put(new RevisionTransportCommand(id: 2), doc)
        assertEquals(1, cache.size())
        assertArrayEquals([doc].toArray(), cache.values().toArray())
        // test for cache size of ten
        cache = new SbmlCache<RevisionTransportCommand, SBMLDocument>(10)
        assertTrue(cache.isEmpty())
        (1..10).each { i ->
            cache.put(new RevisionTransportCommand(id: i), new SBMLDocument())
            assertEquals(i, cache.size())
        }
        assertEquals(10, cache.size())
        cache.put(new RevisionTransportCommand(id: 100), new SBMLDocument())
        assertEquals(10, cache.size())
    }

    void testPut() {
        // tests that put works correctly
        SBMLDocument doc = new SBMLDocument()
        SbmlCache<RevisionTransportCommand, SBMLDocument> cache = new SbmlCache<RevisionTransportCommand, SBMLDocument>(1)
        assertNull(cache.put(new RevisionTransportCommand(id: 1), doc))
        assertSame(doc, cache.put(new RevisionTransportCommand(id: 1), new SBMLDocument()))
        assertNull(cache.put(new RevisionTransportCommand(id: 2), new SBMLDocument()))
        assertNotNull(cache.put(new RevisionTransportCommand(id: 2), new SBMLDocument()))
    }

    void testPutAll() {
        SbmlCache<RevisionTransportCommand, SBMLDocument> cache = new SbmlCache<RevisionTransportCommand, SBMLDocument>(5)
        Map<RevisionTransportCommand, SBMLDocument> entries = [:]
        (1..5).each { i ->
            entries.put(new RevisionTransportCommand(id: i), new SBMLDocument())
        }
        assertTrue(cache.isEmpty())
        cache.putAll entries
        assertEquals(5, cache.size())
        (1..6).each { i ->
            entries.put(new RevisionTransportCommand(id: i), new SBMLDocument())
        }
        cache.putAll entries
        assertEquals(5, cache.size())
    }

    void testContains() {
        SbmlCache<RevisionTransportCommand, SBMLDocument> cache = new SbmlCache<RevisionTransportCommand, SBMLDocument>(5)
        SBMLDocument doc = new SBMLDocument()
        assertFalse(cache.containsKey(new RevisionTransportCommand(id: 1)))
        assertFalse(cache.containsValue(doc))
        RevisionTransportCommand revision = new RevisionTransportCommand(id: 1)
        cache.put(revision, doc)
        assertTrue(cache.containsKey(revision))
        assertTrue(cache.containsKey(new RevisionTransportCommand(id: 1)))
        assertTrue(cache.containsValue(doc))
        cache.put(new RevisionTransportCommand(id: 1), new SBMLDocument(3, 1))
        assertTrue(cache.containsKey(revision))
        assertFalse(cache.containsValue(doc))
        cache.put(new RevisionTransportCommand(id: 2), doc)
        assertTrue(cache.containsKey(revision))
        assertTrue(cache.containsKey(new RevisionTransportCommand(id: 2)))
        assertTrue(cache.containsValue(doc))
    }

    void testRemove() {
        SbmlCache<RevisionTransportCommand, SBMLDocument> cache = new SbmlCache<RevisionTransportCommand, SBMLDocument>(1)
        SBMLDocument doc = new SBMLDocument()
        assertNull(cache.remove(new RevisionTransportCommand(id: 1)))
        cache.put(new RevisionTransportCommand(id: 1), doc)
        assertSame(doc, cache.remove(new RevisionTransportCommand(id: 1)))
        assertNull(cache.remove(new RevisionTransportCommand(id: 1)))
        cache.put(new RevisionTransportCommand(id: 1), doc)
        cache.put(new RevisionTransportCommand(id: 2), doc)
        assertNull(cache.remove(new RevisionTransportCommand(id: 1)))
        assertFalse(cache.isEmpty())
    }

    void testEntries() {
        SbmlCache<RevisionTransportCommand, SBMLDocument> cache = new SbmlCache<RevisionTransportCommand, SBMLDocument>(5)
        SBMLDocument doc = new SBMLDocument()
        SBMLDocument doc1 = new SBMLDocument(3, 1)
        SBMLDocument doc2 = new SBMLDocument(2, 1)
        SBMLDocument doc3 = new SBMLDocument(2, 2)
        SBMLDocument doc4 = new SBMLDocument(2, 3)
        SBMLDocument doc5 = new SBMLDocument(2, 4)
        cache.put(new RevisionTransportCommand(id: 1), doc)
        cache.put(new RevisionTransportCommand(id: 2), doc1)
        cache.put(new RevisionTransportCommand(id: 3), doc2)
        cache.put(new RevisionTransportCommand(id: 4), doc3)
        cache.put(new RevisionTransportCommand(id: 5), doc4)
        cache.put(new RevisionTransportCommand(id: 6), doc5)
        assertArrayEquals([doc1, doc2, doc3, doc4, doc5].toArray(), cache.values().toArray())
        assertArrayEquals([2L, 3L, 4L, 5L, 6L].toArray(), cache.keySet().toList().sort { it.id }.collect { it.id }.toArray())
        List<Entry<RevisionTransportCommand, SBMLDocument>> entries = cache.entrySet().toList().sort { it.key.id }
        assertArrayEquals([2L, 3L, 4L, 5L, 6L].toArray(), entries.collect { it.key.id }.toArray())
        assertArrayEquals([doc1, doc2, doc3, doc4, doc5].toArray(), entries.collect { it.value }.toArray())
        assertSame(doc1, cache.get(new RevisionTransportCommand(id: 2)))
        assertSame(doc2, cache.get(new RevisionTransportCommand(id: 3)))
        assertSame(doc3, cache.get(new RevisionTransportCommand(id: 4)))
        assertSame(doc4, cache.get(new RevisionTransportCommand(id: 5)))
        assertSame(doc5, cache.get(new RevisionTransportCommand(id: 6)))
    }
}
