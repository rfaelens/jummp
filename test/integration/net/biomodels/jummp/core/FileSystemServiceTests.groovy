/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
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
* Apache Commons, JUnit (or a modified version of that library), containing parts
* covered by the terms of Common Public License, Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Commons, JUnit used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.core

import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import java.util.UUID
import org.apache.commons.io.FileUtils
import org.junit.*
import static org.junit.Assert.*

@TestMixin(IntegrationTestMixin)
class FileSystemServiceTests {
    def fileSystemService
    def grailsApplication
    def parentLocation

    @Override
    @Before
    void setUp() {
        parentLocation = new File("target/workingDirectory/")
        FileUtils.deleteDirectory(parentLocation)
        assertTrue(parentLocation.mkdir())
        fileSystemService.root = parentLocation
        fileSystemService.currentModelContainer = parentLocation.absolutePath+File.separator+"ttt"
        fileSystemService.maxContainerSize = 10
        }

    @Override
    @After
    void tearDown() {
        FileUtils.deleteQuietly(parentLocation)
    }

    @Test
    void testCreateParent() {
        assertTrue(parentLocation.exists())
    }

    @Test
    void testContainerPatterns() {
        assertTrue(fileSystemService.findCurrentModelContainer().endsWith("ttt"))
        mockModelFolders(1)
        assertTrue(fileSystemService.findCurrentModelContainer().endsWith("ttt"))
        mockModelFolders(10)
        assertTrue(fileSystemService.findCurrentModelContainer().endsWith("ttu"))
        mockModelFolders(9)
        assertTrue(fileSystemService.findCurrentModelContainer().endsWith("ttv"))
    }


    private void mockModelFolders(final int count) {
        String modelSuffix
        count.times { it ->
            StringBuilder sb = new StringBuilder(fileSystemService.findCurrentModelContainer())
            sb.append(File.separator).append(UUID.randomUUID().toString())
            File m = new File(sb.toString())
            boolean result = m.mkdirs()
        }
    }
}
