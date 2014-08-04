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
class TestSubmitInvalidPharmml extends CreateBase {
    String model = "M1"
    def modelService
    
    TestSubmitInvalidPharmml(def ctx) {
        super()
        modelService = ctx.getBean("modelService")
    }

    //String getModel() { return modelId }

    void performTest() {
        def viewSelection = startFlow()
        assertFlowState("displayDisclaimer")
        signalEvent("Continue")
        assertFlowState("uploadFiles")
        Map<File, String> additionalFiles = getRandomAdditionalFiles(10)
    	File file = new File("test/files/invalidPharmml.xml");
        addSubmissionFiles([file], additionalFiles)
        signalEvent("Upload")
        assertFlowState("uploadFiles")
        def errors = flowScope.workingMemory.get("validationErrorList")
        assertNotNull(errors)
        assertEquals(1, errors.size())
    }
}
