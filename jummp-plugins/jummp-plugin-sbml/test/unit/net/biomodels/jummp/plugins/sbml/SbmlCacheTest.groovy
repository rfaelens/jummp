/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* JSBML, Grails (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, GNU LGPL v2.1, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of JSBML, Grails used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.plugins.sbml

import java.util.Map.Entry
import net.biomodels.jummp.core.model.RevisionTransportCommand
import org.junit.Test
import org.sbml.jsbml.SBMLDocument
import static org.junit.Assert.*

/**
 * Created by IntelliJ IDEA.
 * User: graessli
 * Date: May 2, 2011
 * Time: 11:48:22 AM
 * To change this template use File | Settings | File Templates.
 */
class SbmlCacheTest {

    @Test
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

    @Test
    void testPut() {
        // tests that put works correctly
        SBMLDocument doc = new SBMLDocument()
        SbmlCache<RevisionTransportCommand, SBMLDocument> cache = new SbmlCache<RevisionTransportCommand, SBMLDocument>(1)
        assertNull(cache.put(new RevisionTransportCommand(id: 1), doc))
        assertSame(doc, cache.put(new RevisionTransportCommand(id: 1), new SBMLDocument()))
        assertNull(cache.put(new RevisionTransportCommand(id: 2), new SBMLDocument()))
        assertNotNull(cache.put(new RevisionTransportCommand(id: 2), new SBMLDocument()))
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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
