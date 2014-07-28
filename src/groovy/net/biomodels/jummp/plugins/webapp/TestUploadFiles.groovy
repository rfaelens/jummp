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

import static org.junit.Assert.*
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand as RTC
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision

/* Base class for the classes testing upload pipeline. Navigates to the 
 * upload files page, then allows implementing classes to define further
 * behaviour. Includes a method for clicking through the pipeline with
 * supplied files
 * */
abstract class TestUploadFiles extends CreateBase {
    String model = "M1"
    def modelService

    TestUploadFiles(def ctx) {
        super()
        modelService = ctx.getBean("modelService")
    }

    //String getModel() { return modelId }

    void performTest() {
        def viewSelection = startFlow()
        assertFlowState("displayDisclaimer")
        signalEvent("Continue")
        assertFlowState("uploadFiles")
        performRemainingTest()
    }
    // What the concrete class wants to test
    abstract void performRemainingTest()

    // Click through the upload pipeline with the supplied file and test name/description strings
    void fileUploadPipeline(File file, String format, String mname, String[] descriptionStrings) {
        Map<File, String> additionalFiles = getRandomAdditionalFiles(10)
        addSubmissionFiles([file], additionalFiles)
        signalEvent("Upload")
        assertFlowState("displayModelInfo")
        signalEvent("Continue")

        //Dont add publication info
        assertFlowState("enterPublicationLink")
        //(mockRequest as MockHttpServletRequest).setParameter("PubLinkProvider","PUBMED") Doesnt seem to work :/
        //(mockRequest as MockHttpServletRequest).setParameter("PublicationLink","9486845")

        signalEvent("Continue")

        assertFalse((Boolean) flowScope.workingMemory.get("isUpdateOnExistingModel"))
        assertEquals format, flowScope.workingMemory.get("model_type").identifier
        RTC revision = flowScope.workingMemory.get("RevisionTC") as RTC
        //test name
        assertEquals mname, revision.name
        //test that the description contains known strings
        checkDescription(revision.description, descriptionStrings)
        //add tests for when displayModelInfo does something interesting
        assertFlowState("displaySummaryOfChanges")

        signalEvent("Continue")

        assertFlowExecutionOutcomeEquals("displayConfirmationPage")

        //test that the model is infact saved in the database
        model = mockRequest.session.result_submission as String
        Model thisModel = Model.findByPerennialIdentifier(model)
        assertNotNull thisModel
        Revision rev = modelService.getLatestRevision(thisModel)
        assertNotNull rev
        assertEquals mname, rev.name
        checkDescription(rev.description, descriptionStrings)

        //test that the model is saved in the repository
        List<RepositoryFileTransportCommand> files =
                    modelService.retrieveModelFiles(thisModel)
        validateFiles(files, [file] + additionalFiles.keySet())
    }

    private void checkDescription(String description, String[] descriptionStrings) {
        if (descriptionStrings) {
            descriptionStrings.each {
                assertTrue description.contains(it)
            }
        }
    }
}
