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
 **/



package net.biomodels.jummp.plugins.webapp
import grails.test.WebFlowTestCase
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockMultipartHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.webapp.ModelController

/**
 * WebFlowTestCase seems unable to cope with branching(different routes)
 * in the same test class. Therefore theres multiple classes implementing
 * the routes we want to be able to test
 *
 * Update: See SetupControllerIntegrationTests.groovy for a solution. Current
 * implementation left in to avoid recoding. The issue with the current version
 * is unnecessary instantiation of flows (just one class could be used). On
 * the other hand it is a little bit more modular than it might otherwise have
 * been. Raza Ali: 18/6/13
 * 
 * Base class for the different webflow tests for both create and update
 */

abstract class FlowBase extends WebFlowTestCase {
    abstract def getFlow(); 
    abstract void performTest()

    /*
     * WebFlowTestCase doesnt support file uploads. Modify request
     * property to change that.
     */
    protected void setUp() {
        super.setUp()
        mockRequest = new MockMultipartHttpServletRequest()
        RequestContextHolder.setRequestAttributes(new GrailsWebRequest(mockRequest,mockResponse,mockServletContext,applicationContext))
        registerFlow("model/upload", new ModelController().uploadFlow)
    }

    /* Main Template method for testing. */
    public void testrun() {
        setUp()
        performTest()
        tearDown()
    }

    /* Clicks cancel, checks that flow is aborted. */
    protected void clickCancelEndFlow() {
        signalEvent("Cancel")
        assert "abort" == flowExecutionOutcome.id
    }

    /* Checks the current state against the supplied state id */
    protected void assertFlowState(String state) {
        assert state == flowExecution.activeSession.state.id
    }

    /* 
     * Convenience function to add the supplied main and additional files
     * to submission
     */
    protected void addSubmissionFiles(List<File> mainFiles, Map<File, String> additionalFiles) {
        mainFiles.each {
            addFileToRequest(it, "mainFile", "application/xml")
        }
        additionalFiles.keySet().each {
            addFileToRequest(it, "extraFiles", "application/xml")
            (mockRequest as MockHttpServletRequest).addParameter("description", additionalFiles.get(it))
        }
    }

    /* 
     * Adds the supplied file with parameters as a mock multipart file 
     * 
     */
    private void addFileToRequest(File modelFile, String formID, String contentType) {
        final file = new MockMultipartFile(formID, modelFile.getName(), contentType, modelFile.getBytes())
        (mockRequest as MockMultipartHttpServletRequest).addFile(file)
    }

    /*
     * Convenience function to create arbitrary additional files with corresponding
     * descriptions. 
     */
    protected Map<File,String> getRandomAdditionalFiles(int num) {
        Map<File,String> returnMe=new HashMap<File,String>()
        for (int i=0; i<num; i++) {
            returnMe.put(getFileForTest("add_file_"+i+".xml", "my text is "+num),
            "this is a description for file "+i)
        }
        return returnMe
    }

    /*
     * Convenience function to compare a map of String->byte[] retrieved from
     * the repository with the supplied list of files
     */
    protected void validateFiles(List<RepositoryFileTransportCommand> retrieved, List<File> testFiles) {
        assert retrieved
        Map<String,byte[]> files=new HashMap<String,byte[]>()
        retrieved.each {
            File file=new File(it.path)
            files.put(file.getName(), file.getBytes())
        }
        testFiles.each {
            assert files.containsKey(it.getName())
            byte[] savedFile=files.get(it.getName())
            assert savedFile == it.getBytes()
        }
    }


    public File getFileForTest(String filename, String text) {
        def tmp = System.getProperty("java.io.tmpdir")
        def testFile=new File(tmp + File.separator + filename)
        testFile.setText(text?: "")
        return testFile
    }

    private File bigModel() {
        return new File("test/files/BIOMD0000000272.xml")
    }
}

