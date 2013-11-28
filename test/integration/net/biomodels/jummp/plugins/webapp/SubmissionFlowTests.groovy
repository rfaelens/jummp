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
* groovy, Apache Commons, Spring Framework, Grails, JUnit 
* (or a modified version of that library), containing parts covered by the terms 
* of Common Public License, Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of groovy, Apache Commons, 
* Spring Framework, Grails, JUnit used as well as that of the covered work.}
**/





package net.biomodels.jummp.plugins.webapp

import grails.test.WebFlowTestCase
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import grails.util.Holders
import net.biomodels.jummp.core.JummpIntegrationTest
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand as RTC
import net.biomodels.jummp.core.plugins.webapp.*
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.webapp.ModelController
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.*
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockMultipartHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import static org.junit.Assert.*

class SubmissionFlowTests extends JummpIntegrationTest {
    
    def grailsApplication=Holders.grailsApplication
    def fileSystemService=Holders.applicationContext.getBean("fileSystemService")
    def modelService=Holders.applicationContext.getBean("modelService")
    
    @Before
    void setUp() {
        super.createUserAndRoles()
        File exchangeDirectory = new File("target/vcs/exchange/")
        exchangeDirectory.mkdirs()
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange/"
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/git/"
        File parentLocation = new File(grailsApplication.config.jummp.vcs.workingDirectory )
        parentLocation.mkdirs()
        fileSystemService.root = parentLocation
        fileSystemService.currentModelContainer = parentLocation.absolutePath + File.separator + "ttt"
    }

    @After
    void tearDown() {
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
    }

    /* Aborts at the first step */
    @Test
    void testDisclaimerAbort() {
        assertNotNull(authenticateAsTestUser())
        new TestDisclaimerAbort().testrun()
    }

    /* Continues after the disclaimer */
    @Test
    void testDisclaimerContinue() {
        assertNotNull(authenticateAsTestUser())
        new TestDisclaimerContinue().testrun()
    }

    /* Tests upload page, then clicks abort
     */ 
    @Test
    void testUploadFilesAbort() {
        assertNotNull(authenticateAsTestUser())
        new TestUploadFilesCancel().testrun()
    }

    /* Tests upload pipeline, first with an empty list, 
     * then with an unknown model */
    @Test
    void testUploadFilesContinue() {
        assertNotNull(authenticateAsTestUser())
        TestUploadFilesContinue continued=new TestUploadFilesContinue()
        continued.testrun()
    }

    @Test
    void testUpdateUploadedModel() {
        assertNotNull(authenticateAsAdmin())
        ModelTransportCommand meta = new ModelTransportCommand(comment: "Test Comment", name: "test", format: new ModelFormatTransportCommand(identifier: "UNKNOWN"))
        File importFile = new File("target/vcs/exchange/import.xml")
        FileUtils.touch(importFile)
        importFile.append("Test\n")
        def rf = new RepositoryFileTransportCommand(path: importFile.absolutePath, description: "")
        Model model = modelService.uploadModelAsFile(rf, meta)
        assertTrue(model.validate())
        new TestUpdateSbml(model.id, importFile).testrun()
    }

    /* Tests upload pipeline, first with an empty list, 
     * then with a known SBML model */
    @Test
    void testSubmitSBML() {
        assertNotNull(authenticateAsTestUser())
        new TestSubmitSBML().testrun()
    }

    @Test
    void testSubmitOmex() {
        authenticateAsTestUser()
        new TestSubmitOmex().testrun()
    }

   
}
